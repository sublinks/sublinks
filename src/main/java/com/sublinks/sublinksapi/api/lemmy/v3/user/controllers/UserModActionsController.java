package com.sublinks.sublinksapi.api.lemmy.v3.user.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.GetReportCount;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.ModlogActionType;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.services.ModerationLogService;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.GetSiteResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.BanPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.BanPersonResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.BlockPersonResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GetReportCountResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.services.LemmyPersonService;
import com.sublinks.sublinksapi.authorization.services.AuthorizationService;
import com.sublinks.sublinksapi.comment.repositories.CommentReportRepository;
import com.sublinks.sublinksapi.comment.services.CommentService;
import com.sublinks.sublinksapi.community.dto.Community;
import com.sublinks.sublinksapi.community.repositories.CommunityRepository;
import com.sublinks.sublinksapi.moderation.dto.ModerationLog;
import com.sublinks.sublinksapi.person.dto.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.person.services.LinkPersonCommunityService;
import com.sublinks.sublinksapi.post.repositories.PostReportRepository;
import com.sublinks.sublinksapi.post.services.PostService;
import com.sublinks.sublinksapi.privatemessages.repositories.PrivateMessageReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v3/user")
@Tag(name = "User")
public class UserModActionsController extends AbstractLemmyApiController {

  private final CommentReportRepository commentReportRepository;
  private final PostReportRepository postReportRepository;
  private final PrivateMessageReportRepository privateMessageReportRepository;
  private final CommunityRepository communityRepository;
  private final LinkPersonCommunityService linkPersonCommunityService;
  private final AuthorizationService authorizationService;
  private final PersonRepository personRepository;
  private final PostService postService;
  private final CommentService commentService;
  private final LemmyPersonService lemmyPersonService;
  private final ModerationLogService moderationLogService;

  @Operation(summary = "Ban a person from your site.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BanPersonResponse.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseStatusException.class))),
      @ApiResponse(responseCode = "404", description = "Person not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseStatusException.class)))})
  @PostMapping("ban")
  BanPersonResponse ban(@Valid @RequestBody final BanPerson banPersonForm, JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    authorizationService.isAdminElseThrow(person,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "not_admin"));

    final Person personToBan = personRepository.findById((long) banPersonForm.person_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    personToBan.setBanned(banPersonForm.ban());

    if (banPersonForm.remove_data()) {
      postService.removeAllPostsFromUser(personToBan, true);
      commentService.removeAllCommentsFromUser(personToBan, true);
    }

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModBan)
        .banned(banPersonForm.ban())
        .entityId(personToBan.getId())
        .instance(personToBan.getInstance())
        .moderationPersonId(person.getId())
        .otherPersonId(personToBan.getId())
        .reason(banPersonForm.reason())
        .expires(banPersonForm.expires() == null ? null : new Date(banPersonForm.expires() * 1000L))
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return BanPersonResponse.builder().banned(banPersonForm.ban())
        .person_view(lemmyPersonService.getPersonView(personToBan)).build();
  }

  @Operation(summary = "Block a person.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BlockPersonResponse.class))})})
  @PostMapping("block")
  BlockPersonResponse block() {

    return BlockPersonResponse.builder().build();
  }

  @Operation(summary = "Get counts for your reports.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetReportCountResponse.class))}),
      @ApiResponse(responseCode = "404", description = "Community not found", content = @Content)})
  @GetMapping("report_count")
  GetReportCountResponse reportCount(@Valid final GetReportCount getReportCount,
      JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    final GetReportCountResponse.GetReportCountResponseBuilder builder = GetReportCountResponse.builder();

    if (getReportCount.community_id() != null) {
      final Community community = communityRepository.findById((long) getReportCount.community_id())
          .orElseThrow(
              () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "coomunity_not_found"));

      final boolean isModOfCommunity =
          linkPersonCommunityService.hasLink(person, community, LinkPersonCommunityType.moderator)
              || linkPersonCommunityService.hasLink(person, community,
              LinkPersonCommunityType.owner);

      if (!isModOfCommunity) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
      }

      builder.comment_reports(
          (int) commentReportRepository.countAllCommentReportsByResolvedFalseAndCommunity(
              List.of(community)));
      builder.post_reports(
          (int) postReportRepository.countAllPostReportsByResolvedFalseAndCommunity(
              List.of(community)));
      builder.private_message_reports(0);
    } else {

      final boolean isAdmin = authorizationService.isAdmin(person);

      if (isAdmin) {
        builder.comment_reports(
            (int) commentReportRepository.countAllCommentReportsByResolvedFalse());
        builder.post_reports(
            (int) postReportRepository.countAllPostReportsReportsByResolvedFalse());
        builder.private_message_reports(
            (int) privateMessageReportRepository.countAllPrivateMessageReportsByResolvedFalse());
      } else {
        List<Community> communities = new ArrayList<>();

        communities.addAll(linkPersonCommunityService.getPersonLinkByType(person,
            LinkPersonCommunityType.moderator));
        communities.addAll(
            linkPersonCommunityService.getPersonLinkByType(person, LinkPersonCommunityType.owner));

        builder.comment_reports(
            (int) commentReportRepository.countAllCommentReportsByResolvedFalseAndCommunity(
                communities));
        builder.post_reports(
            (int) postReportRepository.countAllPostReportsByResolvedFalseAndCommunity(communities));

        builder.private_message_reports(0);
      }
    }

    return builder.build();
  }

  @Operation(summary = "Leave the Site admins.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetSiteResponse.class))})})
  @PostMapping("leave_admin")
  GetSiteResponse leaveAdmin() {

    throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
  }
}
