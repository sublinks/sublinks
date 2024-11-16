package com.sublinks.sublinksapi.api.lemmy.v3.post.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.ModlogActionType;
import com.sublinks.sublinksapi.api.lemmy.v3.errorhandler.ApiError;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModLockPost;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemovePost;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.services.ModerationLogService;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.FeaturePost;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.ListPostReports;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.ListPostReportsResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.PostReportResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.PostReportView;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.PostResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.ResolvePostReport;
import com.sublinks.sublinksapi.api.lemmy.v3.post.services.LemmyPostReportService;
import com.sublinks.sublinksapi.api.lemmy.v3.post.services.LemmyPostService;
import com.sublinks.sublinksapi.api.lemmy.v3.utils.PaginationControllerUtils;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPostTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.moderation.entities.ModerationLog;
import com.sublinks.sublinksapi.person.entities.LinkPersonCommunity;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.services.LinkPersonCommunityService;
import com.sublinks.sublinksapi.post.entities.Post;
import com.sublinks.sublinksapi.post.entities.PostReport;
import com.sublinks.sublinksapi.post.models.PostReportSearchCriteria;
import com.sublinks.sublinksapi.post.repositories.PostReportRepository;
import com.sublinks.sublinksapi.post.repositories.PostRepository;
import com.sublinks.sublinksapi.post.services.PostReportService;
import com.sublinks.sublinksapi.post.services.PostService;
import com.sublinks.sublinksapi.shared.RemovedState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller class for performing post moderation actions.
 */
@AllArgsConstructor
@RestController
@Transactional
@RequestMapping(path = "/api/v3/post")
@Tag(name = "Post")
public class PostModActionsController extends AbstractLemmyApiController {

  private final PostReportService postReportService;
  private final LemmyPostReportService lemmyPostReportService;
  private final PostReportRepository postReportRepository;
  private final RolePermissionService rolePermissionService;
  private final LinkPersonCommunityService linkPersonCommunityService;
  private final PostRepository postRepository;
  private final LemmyPostService lemmyPostService;
  private final PostService postService;
  private final ModerationLogService moderationLogService;

  @Operation(summary = "A moderator pin for a post.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PostResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiError.class))})})
  @PostMapping("remove")
  PostResponse remove(@Valid @RequestBody final ModRemovePost modRemovePostForm,
      final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        Set.of(RolePermissionPostTypes.MODERATOR_REMOVE_POST, RolePermissionPostTypes.REMOVE_POST),
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Post post = postRepository.findById(modRemovePostForm.post_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    final boolean isAdmin = rolePermissionService.isPermitted(person,
        RolePermissionPostTypes.REMOVE_POST);

    if (!isAdmin) {
      final boolean moderatesCommunity = linkPersonCommunityService.hasLink(post.getCommunity(),
          person, LinkPersonCommunityType.moderator) || linkPersonCommunityService.hasLink(
          post.getCommunity(), person, LinkPersonCommunityType.owner);

      if (!moderatesCommunity) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
      }
    }

    post.setRemovedState(
        modRemovePostForm.removed() ? RemovedState.REMOVED : RemovedState.NOT_REMOVED);
    postService.updatePost(post);

    postReportService.resolveAllReportsByPost(post, person);

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModRemovePost)
        .removed(modRemovePostForm.removed())
        .entityId(post.getId())
        .postId(post.getId())
        .communityId(post.getCommunity()
            .getId())
        .instance(post.getInstance())
        .moderationPersonId(person.getId())
        .reason(modRemovePostForm.reason())
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return PostResponse.builder()
        .post_view(lemmyPostService.postViewFromPost(post, person))
        .build();
  }

  @Operation(summary = "A moderator lock for a post.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PostResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ResponseStatusException.class))}), @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ResponseStatusException.class))})})
  @PostMapping("lock")
  PostResponse lock(@Valid @RequestBody final ModLockPost modLockPostForm, JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    final Post post = postRepository.findById(modLockPostForm.post_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));

    final boolean isAdmin = RolePermissionService.isAdmin(person);

    if (!isAdmin) {
      final boolean moderatesCommunity = linkPersonCommunityService.hasLink(post.getCommunity(),
          person, LinkPersonCommunityType.moderator) || linkPersonCommunityService.hasLink(
          post.getCommunity(), person, LinkPersonCommunityType.owner);

      if (!moderatesCommunity) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
      }
    }

    post.setLocked(modLockPostForm.locked());
    postService.updatePost(post);

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModLockPost)
        .locked(modLockPostForm.locked())
        .entityId(post.getId())
        .postId(post.getId())
        .communityId(post.getCommunity()
            .getId())
        .instance(post.getInstance())
        .moderationPersonId(person.getId())
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return PostResponse.builder()
        .post_view(lemmyPostService.postViewFromPost(post))
        .build();
  }

  @Operation(summary = "A moderator feature for a post (Sticky).")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PostResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiError.class))})})
  @PostMapping("feature")
  PostResponse feature(@Valid @RequestBody FeaturePost featurePostForm, JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    final Post post = postRepository.findById((long) featurePostForm.post_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    final boolean isAdmin = RolePermissionService.isAdmin(person);

    if (!isAdmin) {
      final boolean moderatesCommunity = linkPersonCommunityService.hasLink(post.getCommunity(),
          person, LinkPersonCommunityType.moderator) || linkPersonCommunityService.hasLink(
          post.getCommunity(), person, LinkPersonCommunityType.owner);

      if (!moderatesCommunity) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
      }
    }
    switch (featurePostForm.feature_type()) {
      case Community:
        post.setFeaturedInCommunity(featurePostForm.featured());
        break;
      case Local:
        if (!isAdmin) {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
        }
        post.setFeatured(featurePostForm.featured());
        break;
      default:
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_feature_type");
    }

    postService.updatePost(post);

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModFeaturePost)
        .featured(post.isFeatured())
        .featuredCommunity(post.isFeaturedInCommunity())
        .communityId(post.getCommunity()
            .getId())
        .entityId(post.getId())
        .postId(post.getId())
        .instance(post.getInstance())
        .moderationPersonId(person.getId())
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return PostResponse.builder()
        .post_view(lemmyPostService.postViewFromPost(post))
        .build();
  }

  @Operation(summary = "Resolve a post report. Only a mod can do this.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PostReportResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ResponseStatusException.class))}), @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ResponseStatusException.class))})})
  @PutMapping("report/resolve")
  PostReportResponse reportResolve(
      @Valid @RequestBody final ResolvePostReport resolvePostReportForm,
      final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    final boolean isAdmin = RolePermissionService.isAdmin(person);

    final PostReport postReport = postReportRepository.findById(
            (long) resolvePostReportForm.report_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    if (!isAdmin) {
      final boolean moderatesCommunity = linkPersonCommunityService.hasLink(postReport.getPost()
          .getCommunity(), person, LinkPersonCommunityType.moderator)
          || linkPersonCommunityService.hasLink(postReport.getPost()
          .getCommunity(), person, LinkPersonCommunityType.owner);

      if (!moderatesCommunity) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
      }
    }

    postReport.setResolved(resolvePostReportForm.resolved());
    postReport.setResolver(person);

    postReportService.updatePostReport(postReport);

    return PostReportResponse.builder()
        .post_report_view(
            lemmyPostReportService.postReportViewFromPost(postReport, postReport.getCreator()))
        .build();
  }

  @Operation(summary = "List post reports.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ListPostReportsResponse.class))}), @ApiResponse(
      responseCode = "404",
      description = "Post Not Found",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ResponseStatusException.class))}), @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ResponseStatusException.class))})})
  @GetMapping("report/list")
  ListPostReportsResponse reportList(@Valid final ListPostReports listPostReportsForm,
      final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    final boolean isAdmin = RolePermissionService.isAdmin(person);

    final List<PostReport> postReports = new ArrayList<>();
    final int page = PaginationControllerUtils.getAbsoluteMinNumber(listPostReportsForm.page(), 1);
    final int perPage = PaginationControllerUtils.getAbsoluteMinNumber(listPostReportsForm.limit(),
        20);

    if (!isAdmin) {
      final List<Community> moderatingCommunities = new ArrayList<>();
      moderatingCommunities.addAll(linkPersonCommunityService.getLinks(person,
              LinkPersonCommunityType.owner)
          .stream()
          .map(LinkPersonCommunity::getCommunity)
          .toList());

      moderatingCommunities.addAll(linkPersonCommunityService.getLinks(person,
              LinkPersonCommunityType.moderator)
          .stream()
          .map(LinkPersonCommunity::getCommunity)
          .toList());

      postReports.addAll(postReportRepository.allPostReportsBySearchCriteria(
          PostReportSearchCriteria.builder()
              .unresolvedOnly(listPostReportsForm.unresolved_only() == null
                  || listPostReportsForm.unresolved_only())
              .perPage(perPage)
              .page(page)
              .community(moderatingCommunities)
              .build()));
    } else {
      postReports.addAll(postReportRepository.allPostReportsBySearchCriteria(
          PostReportSearchCriteria.builder()
              .unresolvedOnly(listPostReportsForm.unresolved_only() != null
                  && listPostReportsForm.unresolved_only())
              .perPage(perPage)
              .page(page)
              .build()));
    }

    List<PostReportView> postReportView = new ArrayList<>();

    for (PostReport postReport : postReports) {
      postReportView.add(lemmyPostReportService.postReportViewFromPost(postReport, person));
    }

    return ListPostReportsResponse.builder()
        .post_reports(postReportView)
        .build();
  }
}
