package com.sublinks.sublinksapi.api.lemmy.v3.post.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.comment.models.VoteView;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityModeratorView;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.community.services.LemmyCommunityService;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.ListingType;
import com.sublinks.sublinksapi.api.lemmy.v3.errorhandler.ApiError;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.CreatePostLike;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.CreatePostReport;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.GetPost;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.GetPostResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.GetPosts;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.GetPostsResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.ListPostLikes;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.ListPostLikesResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.MarkPostAsRead;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.PostReportResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.PostResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.PostView;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.SavePost;
import com.sublinks.sublinksapi.api.lemmy.v3.post.services.LemmyPostReportService;
import com.sublinks.sublinksapi.api.lemmy.v3.post.services.LemmyPostService;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.GetSiteMetadata;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.GetSiteMetadataResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.SiteMetadata;
import com.sublinks.sublinksapi.api.lemmy.v3.utils.PaginationControllerUtils;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPostTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.comment.entities.Comment;
import com.sublinks.sublinksapi.comment.repositories.CommentRepository;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.community.repositories.CommunityRepository;
import com.sublinks.sublinksapi.instance.entities.InstanceConfig;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.person.entities.LinkPersonCommunity;
import com.sublinks.sublinksapi.person.entities.LinkPersonPost;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.enums.LinkPersonPostType;
import com.sublinks.sublinksapi.person.enums.SortType;
import com.sublinks.sublinksapi.person.services.LinkPersonPostService;
import com.sublinks.sublinksapi.post.entities.Post;
import com.sublinks.sublinksapi.post.entities.PostLike;
import com.sublinks.sublinksapi.post.entities.PostReport;
import com.sublinks.sublinksapi.post.models.PostSearchCriteria;
import com.sublinks.sublinksapi.post.repositories.PostRepository;
import com.sublinks.sublinksapi.post.services.PostLikeService;
import com.sublinks.sublinksapi.post.services.PostReadService;
import com.sublinks.sublinksapi.post.services.PostReportService;
import com.sublinks.sublinksapi.post.sorts.SortFactory;
import com.sublinks.sublinksapi.post.sorts.SortingTypeInterface;
import com.sublinks.sublinksapi.utils.SiteMetadataUtil;
import com.sublinks.sublinksapi.utils.UrlUtil;
import com.sublinks.sublinksapi.utils.models.LemmyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
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
 * Controller class for managing posts in the Lemmy API. Extends the AbstractLemmyApiController
 * class.
 */
@RestController
@RequiredArgsConstructor
@Transactional
@RequestMapping(path = "/api/v3/post")
@Tag(name = "Post")
public class PostController extends AbstractLemmyApiController {

  private final LemmyCommunityService lemmyCommunityService;
  private final LemmyPostService lemmyPostService;
  private final PostLikeService postLikeService;
  private final LinkPersonPostService linkPersonPostService;
  private final CommunityRepository communityRepository;
  private final PostRepository postRepository;
  private final UrlUtil urlUtil;
  private final PostReadService postReadService;
  private final ConversionService conversionService;
  private final SiteMetadataUtil siteMetadataUtil;
  private final PostReportService postReportService;
  private final LemmyPostReportService lemmyPostReportService;
  private final LocalInstanceContext localInstanceContext;
  private final RolePermissionService rolePermissionService;
  private final SortFactory sortFactory;
  private final CommentRepository commentRepository;

  @Operation(summary = "Get / fetch a post.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = GetPostResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiError.class))})})
  @GetMapping
  GetPostResponse show(@Valid final GetPost getPostForm, final JwtPerson principal)
      throws LemmyException {

    Post post = null;

    if (getPostForm.id() != null) {
      post = postRepository.findById((long) getPostForm.id())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    } else {
      post = commentRepository.findById((long) getPostForm.comment_id())
          .map(Comment::getPost)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    final Community community = post.getCommunity();

    Optional<Person> person = getOptionalPerson(principal);

    rolePermissionService.isPermitted(person.orElse(null), RolePermissionPostTypes.READ_POST,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    if (post.isDeleted() || post.isRemoved()) {
      if (person.isPresent()) {
        Person viewer = person.get();

        boolean isCreator = post.getLinkPersonPost()
            .stream()
            .anyMatch(l -> l.getPerson()
                .equals(viewer) && l.getLinkType() == LinkPersonPostType.creator);
        if (!(rolePermissionService.isPermitted(viewer,
            RolePermissionPostTypes.MODERATOR_SHOW_DELETED_POST) && post.getCommunity()
            .getLinkPersonCommunity()
            .stream()
            .anyMatch(l -> l.getPerson()
                .getId()
                .equals(viewer.getId()) && l.getLinkType() == LinkPersonCommunityType.moderator))
            && !rolePermissionService.isPermitted(viewer,
            RolePermissionPostTypes.ADMIN_SHOW_DELETED_POST)) {

          if (!(post.isDeleted() && !post.isRemoved() && isCreator)) {
            if (post.isRemoved()) {
              throw new LemmyException("post_removed_by_mod", HttpStatus.BAD_REQUEST);
            } else {
              throw new LemmyException("post_deleted", HttpStatus.BAD_REQUEST);
            }
          }
        }

        // fall through
      } else {
        if (post.isRemoved()) {
          throw new LemmyException("post_removed_by_mod", HttpStatus.BAD_REQUEST);
        } else {
          throw new LemmyException("post_deleted", HttpStatus.BAD_REQUEST);
        }
      }
    }

    PostView postView;
    final CommunityView communityView;
    if (person.isPresent()) {
      communityView = lemmyCommunityService.communityViewFromCommunity(community, person.get());
      postView = lemmyPostService.postViewFromPost(post, person.get());
      postReadService.markPostReadByPerson(post, person.get());
    } else {
      communityView = lemmyCommunityService.communityViewFromCommunity(community);
      postView = lemmyPostService.postViewFromPost(post);
    }
    final List<CommunityModeratorView> moderators = lemmyCommunityService.communityModeratorViewList(
        community);
    Set<PostView> crossPosts = new LinkedHashSet<>();
    if (post.getCrossPost() != null && post.getCrossPost()
        .getPosts() != null) {
      for (Post crossPostPost : post.getCrossPost()
          .getPosts()) {
        if (post.equals(crossPostPost)) {
          continue;
        }
        if (person.isPresent()) {
          crossPosts.add(lemmyPostService.postViewFromPost(crossPostPost, person.get()));
        } else {
          crossPosts.add(lemmyPostService.postViewFromPost(crossPostPost));
        }
      }
    }

    return GetPostResponse.builder()
        .post_view(postView)
        .community_view(communityView)
        .moderators(moderators)
        .cross_posts(crossPosts)
        .build();
  }

  @Operation(summary = "Mark a post as read.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PostResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiError.class))})})
  @PostMapping("mark_as_read")
  PostResponse markAsRead(@Valid @RequestBody final MarkPostAsRead markPostAsReadForm,
      final JwtPerson principal) {

    final Person person = getPersonOrThrowBadRequest(principal);

    rolePermissionService.isPermitted(person, RolePermissionPostTypes.MARK_POST_AS_READ,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    // @todo support multiple posts
    final Post post = postRepository.findById((long) markPostAsReadForm.post_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    postReadService.markPostReadByPerson(post, person);
    return PostResponse.builder()
        .post_view(lemmyPostService.postViewFromPost(post, person))
        .build();
  }

  @Operation(summary = "Get / fetch posts, with various filters.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = GetPostsResponse.class))})})
  @GetMapping("list")
  @Transactional(readOnly = true)
  public GetPostsResponse index(@Valid final GetPosts getPostsForm, final JwtPerson principal) {

    final Optional<Person> person = getOptionalPerson(principal);

    rolePermissionService.isPermitted(person.orElse(null), RolePermissionPostTypes.READ_POSTS,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    List<Long> communityIds = new ArrayList<>();
    Community community;

    if (getPostsForm.type_() != null) {
      switch (getPostsForm.type_()) {
        case Subscribed -> {
          if (person.isEmpty()) {
            break;
          }
          final Set<LinkPersonCommunity> personCommunities = person.get()
              .getLinkPersonCommunity();
          if (!personCommunities.isEmpty()) {
            for (LinkPersonCommunity l : personCommunities) {
              if (l.getLinkType() == LinkPersonCommunityType.follower) {
                communityIds.add(l.getCommunity()
                    .getId());
              }
            }
          }
        }
        case Local -> {
          for (Community c : communityRepository.findAll()) { // @todo find local
            communityIds.add(c.getId());
          }
        }
        case All, ModeratorView -> {
          // @todo only non deleted communities
          // fall through for now
        }
      }
    }
    if (getPostsForm.type_() == null || getPostsForm.type_() != ListingType.Subscribed) {
      if (getPostsForm.community_name() != null || getPostsForm.community_id() != null) {
        Long communityId =
            getPostsForm.community_id() == null ? null : (long) getPostsForm.community_id();
        community = communityRepository.findCommunityByIdOrTitleSlug(communityId,
            getPostsForm.community_name());
        if (community == null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "community_not_found");
        }
        if (!(getPostsForm.type_() != null && getPostsForm.type_() == ListingType.Local
            && !community.isLocal())) {
          communityIds.add(community.getId());
        }
      }
    }
    InstanceConfig config = localInstanceContext.instance()
        .getInstanceConfig();

    SortType sortType = null; // @todo set to site default
    if (getPostsForm.sort() != null) {
      sortType = conversionService.convert(getPostsForm.sort(), SortType.class);
    }
    if (sortType == null) {
      sortType = SortType.New;
    }
    SortingTypeInterface sorting = sortFactory.createSort(sortType);

    ListingType listingType = config != null ? config.getDefaultPostListingType() : ListingType.All;
    if (getPostsForm.type_() != null) {
      listingType = conversionService.convert(getPostsForm.type_(), ListingType.class);
    }

    final int page = PaginationControllerUtils.getAbsoluteMinNumber(getPostsForm.page(), 1);
    final int perPage = PaginationControllerUtils.getAbsoluteMinNumber(getPostsForm.limit(), 20);

    final PostSearchCriteria postSearchCriteria = PostSearchCriteria.builder()
        .page(page)
        .listingType(conversionService.convert(listingType,
            com.sublinks.sublinksapi.person.enums.ListingType.class))
        .perPage(perPage + 1)
        .isSavedOnly(getPostsForm.saved_only() != null && getPostsForm.saved_only())
        .isDislikedOnly(getPostsForm.disliked_only() != null && getPostsForm.disliked_only())
        .sortType(sortType)
        .person(person.orElse(null))
        .communityIds(!communityIds.isEmpty() ? communityIds : null)
        .cursorBasedPageable(getPostsForm.page_cursor())
        .build();

    final List<Post> posts = postRepository.allPostsBySearchCriteria(postSearchCriteria);

    String cursor = null;
    if (posts.size() > perPage) {
      cursor = sorting.getCursor(posts);
      posts.remove(posts.size() - 1);
    }

    final Collection<PostView> postViewCollection = new LinkedHashSet<>();
    if (person.isPresent()) {
      for (Post post : posts) {
        postViewCollection.add(lemmyPostService.postViewFromPost(post, person.get()));
      }
    } else {
      for (Post post : posts) {
        postViewCollection.add(lemmyPostService.postViewFromPost(post));
      }
    }

    return GetPostsResponse.builder()
        .posts(postViewCollection)
        .next_page(cursor)
        .build();
  }


  @Operation(summary = "Like / vote on a post.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PostResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiError.class))})})
  @PostMapping("like")
  public PostResponse like(@Valid @RequestBody CreatePostLike createPostLikeForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, switch (createPostLikeForm.score()) {
      case 1 -> RolePermissionPostTypes.POST_UPVOTE;
      case -1 -> RolePermissionPostTypes.POST_DOWNVOTE;
      default -> RolePermissionPostTypes.POST_NEUTRALVOTE;
    }, () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Post post = postRepository.findById(createPostLikeForm.post_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    if (createPostLikeForm.score() == 1) {
      postLikeService.updateOrCreatePostLikeLike(post, person);
    } else if (createPostLikeForm.score() == -1) {
      postLikeService.updateOrCreatePostLikeDislike(post, person);
    } else {
      postLikeService.updateOrCreatePostLikeNeutral(post, person);
    }

    return PostResponse.builder()
        .post_view(lemmyPostService.postViewFromPost(post, person))
        .build();
  }

  @Operation(summary = "Get Votes on a post.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PostResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiError.class))})})
  @GetMapping("like/list")
  public ListPostLikesResponse listLikes(@Valid ListPostLikes listPostLikesForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPostTypes.POST_LIST_VOTES,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Post post = postRepository.findById((long) listPostLikesForm.post_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

    List<PostLike> likes = postLikeService.getPostLikes(post,
        PaginationControllerUtils.getAbsoluteMinNumber(listPostLikesForm.page(), 1),
        PaginationControllerUtils.getAbsoluteMinNumber(listPostLikesForm.limit(), 20));

    List<VoteView> voteViews = new ArrayList<>();
    for (PostLike like : likes) {
      voteViews.add(conversionService.convert(like, VoteView.class));
    }

    return ListPostLikesResponse.builder()
        .post_likes(voteViews)
        .build();
  }

  @Operation(summary = "Save a post.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PostResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiError.class))})})
  @PutMapping("save")
  public PostResponse saveForLater(@Valid @RequestBody SavePost savePostForm, JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);
    rolePermissionService.isPermitted(person, RolePermissionPostTypes.FAVORITE_POST,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Post post = postRepository.findById((long) savePostForm.post_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

    if (savePostForm.save()) {
      linkPersonPostService.createPostLink(post, person, LinkPersonPostType.saved);
    } else {
      final Optional<LinkPersonPost> linkPersonPost = linkPersonPostService.getLink(post, person,
          LinkPersonPostType.saved);
      linkPersonPost.ifPresent(linkPersonPostService::deleteLink);
    }
    return PostResponse.builder()
        .post_view(lemmyPostService.postViewFromPost(post, person))
        .build();
  }

  @Operation(summary = "Report a post.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PostReportResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Post Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ResponseStatusException.class))})})
  @PostMapping("report")
  public PostReportResponse report(@Valid @RequestBody final CreatePostReport createPostReportForm,
      final JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPostTypes.REPORT_POST,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Post post = postRepository.findById((long) createPostReportForm.post_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "post_not_found"));

    final PostReport postReport = PostReport.builder()
        .post(post)
        .creator(person)
        .reason(createPostReportForm.reason())
        .originalBody(post.getPostBody() == null ? "" : post.getPostBody())
        .originalTitle(post.getTitle() == null ? "" : post.getTitle())
        .originalUrl(post.getLinkUrl() == null ? "" : post.getLinkUrl())
        .build();

    postReportService.createPostReport(postReport);

    return PostReportResponse.builder()
        .post_report_view(lemmyPostReportService.postReportViewFromPost(postReport, person))
        .build();

  }

  @Operation(summary = "Fetch metadata for any given site.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = GetSiteMetadataResponse.class))})})
  @GetMapping("site_metadata")
  public GetSiteMetadataResponse siteMetadata(@Valid GetSiteMetadata getSiteMetadataForm) {

    String normalizedUrl = urlUtil.normalizeUrl(getSiteMetadataForm.url());
    SiteMetadataUtil.SiteMetadata siteMetadata = siteMetadataUtil.fetchSiteMetadata(normalizedUrl);

    return GetSiteMetadataResponse.builder()
        .metadata(SiteMetadata.builder()
            .title(siteMetadata.title())
            .description(siteMetadata.description())
            .image(siteMetadata.imageUrl())
            .embed_video_url(siteMetadata.videoUrl())
            .build())
        .build();
  }
}
