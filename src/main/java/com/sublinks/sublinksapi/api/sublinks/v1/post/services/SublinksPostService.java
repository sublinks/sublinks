package com.sublinks.sublinksapi.api.sublinks.v1.post.services;

import com.sublinks.sublinksapi.api.sublinks.v1.common.models.RequestResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.AggregatePostResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.CreatePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.DeletePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.IndexPost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.PostResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.UpdatePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation.FavoritePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation.PinPost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation.PurgePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation.RemovePost;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPostTypes;
import com.sublinks.sublinksapi.authorization.services.AclService;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.community.repositories.CommunityRepository;
import com.sublinks.sublinksapi.instance.entities.Instance;
import com.sublinks.sublinksapi.language.entities.Language;
import com.sublinks.sublinksapi.language.repositories.LanguageRepository;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.enums.LinkPersonPostType;
import com.sublinks.sublinksapi.person.enums.ListingType;
import com.sublinks.sublinksapi.person.enums.SortType;
import com.sublinks.sublinksapi.person.repositories.LinkPersonPostRepository;
import com.sublinks.sublinksapi.person.services.LinkPersonCommunityService;
import com.sublinks.sublinksapi.person.services.LinkPersonPostService;
import com.sublinks.sublinksapi.person.services.PersonService;
import com.sublinks.sublinksapi.post.entities.Post;
import com.sublinks.sublinksapi.post.entities.Post.PostBuilder;
import com.sublinks.sublinksapi.post.entities.PostReport;
import com.sublinks.sublinksapi.post.models.PostSearchCriteria;
import com.sublinks.sublinksapi.post.repositories.PostAggregateRepository;
import com.sublinks.sublinksapi.post.repositories.PostRepository;
import com.sublinks.sublinksapi.post.services.PostReportService;
import com.sublinks.sublinksapi.post.services.PostService;
import com.sublinks.sublinksapi.shared.RemovedState;
import com.sublinks.sublinksapi.slurfilter.exceptions.SlurFilterBlockedException;
import com.sublinks.sublinksapi.slurfilter.exceptions.SlurFilterReportException;
import com.sublinks.sublinksapi.slurfilter.services.SlurFilterService;
import com.sublinks.sublinksapi.utils.SiteMetadataUtil;
import com.sublinks.sublinksapi.utils.UrlUtil;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Service
public class SublinksPostService {

  private final PostService postService;
  private final PostRepository postRepository;
  private final ConversionService conversionService;
  private final CommunityRepository communityRepository;
  private final RolePermissionService rolePermissionService;
  private final LinkPersonCommunityService linkPersonCommunityService;
  private final LanguageRepository languageRepository;
  private final Instance localInstance;
  private final SlurFilterService slurFilterService;
  private final UrlUtil urlUtil;
  private final SiteMetadataUtil siteMetadataUtil;
  private final PostReportService postReportService;
  private final PersonService personService;
  private final PostAggregateRepository postAggregateRepository;
  private final LinkPersonPostService linkPersonPostService;
  private final LinkPersonPostRepository linkPersonPostRepository;
  private final AclService aclService;

  /**
   * Retrieves a list of PostResponse objects based on the provided search criteria.
   *
   * @param indexPostForm The IndexPost object containing the search criteria.
   * @param person        The Person object representing the user.
   * @return A list of PostResponse objects matching the search criteria.
   */
  public List<PostResponse> index(final IndexPost indexPostForm, final Person person) {

    aclService.canPerson(person)
        .performTheAction(RolePermissionPostTypes.READ_POSTS)
        .orThrowUnauthorized();

    final List<Community> communities = indexPostForm.communityKeys() == null ? null
        : communityRepository.findCommunityByTitleSlugIn(indexPostForm.communityKeys());

    List<Post> posts = postRepository.allPostsBySearchCriteria(PostSearchCriteria.builder()
        .search(indexPostForm.search())
        .sortType(conversionService.convert(indexPostForm.sortType(), SortType.class))
        .listingType(conversionService.convert(indexPostForm.listingType(), ListingType.class))
        .communityIds(communities == null ? null : communities.stream()
            .map(Community::getId)
            .toList())
        .isShowNsfw(indexPostForm.showNsfw())
        .isSavedOnly(indexPostForm.savedOnly())
        .perPage(indexPostForm.perPage())
        .page(indexPostForm.page())
        .person(person)
        .cursorBasedPageable(indexPostForm.pageCursor())
        .build());

    return posts.stream()
        .map(post -> conversionService.convert(post, PostResponse.class))
        .toList();
  }

  /**
   * Retrieves the PostResponse object for the given key and person.
   *
   * @param key    The key of the post.
   * @param person The person object representing the user.
   * @return The PostResponse object matching the key.
   * @throws ResponseStatusException If the post is not found or the user does not have permission
   *                                 to access the post.
   */
  public PostResponse show(final String key, final Person person) {

    final Post post = postRepository.findByTitleSlug(key)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));

    if (post.isRemoved() || post.isDeleted()) {

      final boolean isPermittedToReadRemovedPosts =
          person != null && (rolePermissionService.isPermitted(person,
              RolePermissionPostTypes.ADMIN_SHOW_DELETED_POST) || (
              rolePermissionService.isPermitted(person,
                  RolePermissionPostTypes.MODERATOR_SHOW_DELETED_POST)
                  && linkPersonCommunityService.hasAnyLink(person, post.getCommunity(),
                  List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))));

      final boolean isOwner = person != null && postService.getPostCreator(post) == person;

      if (!isPermittedToReadRemovedPosts || !(isOwner && !post.isRemoved())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found");
      }
    }

    return conversionService.convert(post, PostResponse.class);
  }

  /**
   * Creates a new Post with the provided form data and person.
   *
   * @param createPostForm The CreatePost object containing the form data for creating the post.
   * @param person         The Person object representing the user who is creating the post.
   * @return The created PostResponse object.
   * @throws ResponseStatusException If the community or feature post permission is not found or
   *                                 denied.
   */
  public PostResponse create(final CreatePost createPostForm, final Person person) {

    aclService.canPerson(person)
        .performTheAction(RolePermissionPostTypes.CREATE_POST)
        .orThrowUnauthorized();

    final Community community = communityRepository.findCommunityByTitleSlug(
            createPostForm.communityKey())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "community_not_found"));

    final Language language = createPostForm.languageKey() != null && !createPostForm.languageKey()
        .isEmpty() ? languageRepository.findLanguageByCode(createPostForm.languageKey())
        : personService.getPersonDefaultPostLanguage(person, community)
            .orElse(languageRepository.findLanguageByCode("und"));

    if (language == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "language_not_found");
    }

    if (community.getLanguages()
        .stream()
        .noneMatch(communityLanguage -> communityLanguage.getCode()
            .equals(language.getCode()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "language_not_supported_by_community");
    }

    if (community.getInstance()
        .getLanguages()
        .stream()
        .noneMatch(instanceLanguage -> instanceLanguage.getCode()
            .equals(language.getCode()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "language_not_supported_by_instance");
    }

    // @todo: modlog?

    if (createPostForm.featuredLocal()) {
      if (!rolePermissionService.isPermitted(person, RolePermissionPostTypes.ADMIN_PIN_POST)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
      }
    }
    if (createPostForm.featuredCommunity()) {
      if (!(rolePermissionService.isPermitted(person, RolePermissionPostTypes.MODERATOR_PIN_POST)
          && linkPersonCommunityService.hasAnyLink(person, community,
          List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner)))
          && !rolePermissionService.isPermitted(person, RolePermissionPostTypes.ADMIN_PIN_POST)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
      }
    }

    final PostBuilder postBuilder = Post.builder()
        .title(createPostForm.title())
        .postBody(createPostForm.body())
        .language(language)
        .community(community)
        .isNsfw(createPostForm.nsfw())
        .isFeatured(createPostForm.featuredLocal())
        .isFeaturedInCommunity(createPostForm.featuredCommunity())
        .instance(localInstance)
        .removedState(RemovedState.NOT_REMOVED);

    boolean shouldReport = false;

    try {
      postBuilder.postBody(slurFilterService.censorText(createPostForm.body()));
    } catch (SlurFilterReportException e) {
      shouldReport = true;
      postBuilder.postBody(createPostForm.body());
    } catch (SlurFilterBlockedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "post_blocked_by_slur_filter");
    }

    try {
      postBuilder.title(slurFilterService.censorText(createPostForm.title()));
    } catch (SlurFilterReportException e) {
      shouldReport = true;
      postBuilder.title(createPostForm.title());
    } catch (SlurFilterBlockedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "post_blocked_by_slur_filter");
    }
    String url = createPostForm.link();
    SiteMetadataUtil.SiteMetadata metadata = null;
    if (url != null) {
      String metadataUrl = urlUtil.normalizeUrl(url);
      urlUtil.checkUrlProtocol(metadataUrl);
      metadata = siteMetadataUtil.fetchSiteMetadata(metadataUrl);
    }

    if (url != null) {
      postBuilder.linkUrl(url);
      if (metadata != null) {
        postBuilder.linkTitle(metadata.title())
            .linkDescription(metadata.description())
            .linkVideoUrl(metadata.videoUrl())
            .linkThumbnailUrl(metadata.imageUrl());
      }
    }

    final Post post = postBuilder.build();
    postService.createPost(post, person);

    if (shouldReport) {
      postReportService.createPostReport(PostReport.builder()
          .post(post)
          .creator(person)
          .reason("AUTOMATED: Post creation triggered a slur filter")
          .originalBody(post.getPostBody() == null ? "" : post.getPostBody())
          .originalTitle(post.getTitle() == null ? "" : post.getTitle())
          .originalUrl(post.getLinkUrl() == null ? "" : post.getLinkUrl())
          .build());
    }

    return conversionService.convert(post, PostResponse.class);
  }

  /**
   * Updates a post with the provided information.
   *
   * @param postKey        The key of the post to update.
   * @param updatePostForm The UpdatePost object containing the updated information.
   * @param person         The Person object representing the user making the update.
   * @return The updated PostResponse object.
   * @throws ResponseStatusException If the post is not found, the language is not supported, or the
   *                                 user does not have permission to update the post.
   */
  public PostResponse update(final String postKey, final UpdatePost updatePostForm,
      final Person person)
  {

    final Post post = postRepository.findByTitleSlug(postKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));
    final Community community = post.getCommunity();

    aclService.canPerson(person)
        .onCommunity(community)
        .performTheAction(RolePermissionPostTypes.UPDATE_POST)
        .orThrowUnauthorized();

    if (updatePostForm.languageKey() != null && !updatePostForm.languageKey()
        .isEmpty()) {

      final Language language = languageRepository.findLanguageByCode(updatePostForm.languageKey());

      if (language == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "language_not_found");
      }

      if (community.getLanguages()
          .stream()
          .noneMatch(communityLanguage -> communityLanguage.getCode()
              .equals(language.getCode()))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "language_not_supported_by_community");
      }

      if (community.getInstance()
          .getLanguages()
          .stream()
          .noneMatch(instanceLanguage -> instanceLanguage.getCode()
              .equals(language.getCode()))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "language_not_supported_by_instance");
      }

      post.setLanguage(language);
    }

    // @todo: modlog?

    if (updatePostForm.featuredLocal() != null) {
      if (!rolePermissionService.isPermitted(person, RolePermissionPostTypes.ADMIN_PIN_POST)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
      }

      post.setFeatured(updatePostForm.featuredLocal());
    }
    if (updatePostForm.featuredCommunity() != null) {
      if (!(rolePermissionService.isPermitted(person, RolePermissionPostTypes.MODERATOR_PIN_POST)
          && linkPersonCommunityService.hasAnyLink(person, post.getCommunity(),
          List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner)))
          && !rolePermissionService.isPermitted(person, RolePermissionPostTypes.ADMIN_PIN_POST)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
      }

      post.setFeatured(updatePostForm.featuredCommunity());
    }

    boolean shouldReport = false;

    if (updatePostForm.title() != null && !updatePostForm.title()
        .isBlank()) {
      try {
        post.setPostBody(slurFilterService.censorText(updatePostForm.body()));
      } catch (SlurFilterReportException e) {
        shouldReport = true;
        post.setPostBody(updatePostForm.body());
      } catch (SlurFilterBlockedException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "post_blocked_by_slur_filter");
      }
    }

    if (updatePostForm.nsfw() != null) {
      post.setNsfw(updatePostForm.nsfw());
    }
    if (updatePostForm.body() != null && !updatePostForm.body()
        .isBlank()) {
      try {
        post.setTitle(slurFilterService.censorText(updatePostForm.title()));
      } catch (SlurFilterReportException e) {
        shouldReport = true;
        post.setTitle(updatePostForm.title());
      } catch (SlurFilterBlockedException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "post_blocked_by_slur_filter");
      }
    }

    String url = updatePostForm.link();
    SiteMetadataUtil.SiteMetadata metadata = null;
    if (url != null) {
      String metadataUrl = urlUtil.normalizeUrl(url);
      urlUtil.checkUrlProtocol(metadataUrl);
      metadata = siteMetadataUtil.fetchSiteMetadata(metadataUrl);
    }

    if (url != null) {
      post.setLinkUrl(url);
      if (metadata != null) {
        post.setLinkUrl(metadata.title());
        post.setLinkDescription(metadata.description());
        post.setLinkVideoUrl(metadata.videoUrl());
        post.setLinkThumbnailUrl(metadata.imageUrl());
      }
    }

    postService.updatePost(post);

    if (shouldReport) {
      postReportService.createPostReport(PostReport.builder()
          .post(post)
          .creator(person)
          .reason("AUTOMATED: Post update triggered a slur filter")
          .originalBody(post.getPostBody() == null ? "" : post.getPostBody())
          .originalTitle(post.getTitle() == null ? "" : post.getTitle())
          .originalUrl(post.getLinkUrl() == null ? "" : post.getLinkUrl())
          .build());
    }

    return conversionService.convert(post, PostResponse.class);
  }

  /**
   * Removes a post from the system.
   *
   * @param postKey        The key of the post to be removed.
   * @param removePostForm The RemovePost object containing the removal information.
   * @param person         The Person object representing the user removing the post.
   * @return The PostResponse object for the removed post.
   * @throws ResponseStatusException If the post is not found, the user is unauthorized, or an error
   *                                 occurs during the removal process.
   */
  public PostResponse remove(final String postKey, final RemovePost removePostForm,
      final Person person)
  {

    final Post post = postRepository.findByTitleSlug(postKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));

    if (!rolePermissionService.isPermitted(person, RolePermissionPostTypes.REMOVE_POST) && !(
        rolePermissionService.isPermitted(person, RolePermissionPostTypes.MODERATOR_REMOVE_POST)
            && linkPersonCommunityService.hasAnyLink(person, post.getCommunity(),
            List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner)))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    post.setRemovedState(removePostForm.remove() ? RemovedState.REMOVED : RemovedState.NOT_REMOVED);

    // @todo: modlog?

    postService.updatePost(post);
    return conversionService.convert(post, PostResponse.class);
  }

  /**
   * Deletes a post with the specified post key, using the provided delete post form and person.
   *
   * @param postKey        The key of the post to delete.
   * @param deletePostForm The DeletePost object containing additional parameters for the deletion.
   * @param person         The Person object representing the user performing the deletion.
   * @return The PostResponse object for the deleted post.
   * @throws ResponseStatusException If the post is not found or the user is not permitted to delete
   *                                 the post.
   */
  public PostResponse delete(final String postKey, final DeletePost deletePostForm,
      final Person person)
  {

    final Post post = postRepository.findByTitleSlug(postKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));

    if (!(aclService.canPerson(person)
        .onCommunity(post.getCommunity())
        .performTheAction(RolePermissionPostTypes.DELETE_POST)
        .isPermitted() && !Objects.equals(postService.getPostCreator(post)
        .getId(), person.getId()) && !post.isRemoved())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    post.setDeleted(deletePostForm.remove());

    // @todo: modlog?

    postService.updatePost(post);
    return conversionService.convert(post, PostResponse.class);
  }

  /**
   * Retrieves the aggregated post information for the given post key and person.
   *
   * @param postKey The key of the post to aggregate.
   * @param person  The Person object representing the user.
   * @return The aggregated post information as an AggregatePostResponse object.
   * @throws ResponseStatusException If the post is not found or the user does not have permission
   *                                 to access the post.
   */
  public AggregatePostResponse aggregate(String postKey, Person person) {

    rolePermissionService.isPermitted(person, RolePermissionPostTypes.READ_POST_AGGREGATE,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    return postAggregateRepository.findByPost_TitleSlug(postKey)
        .map(postAggregate -> conversionService.convert(postAggregate, AggregatePostResponse.class))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));
  }


  public PostResponse favorite(final String postKey, final FavoritePost favoritePostForm,
      final Person person)
  {

    rolePermissionService.isPermitted(person, RolePermissionPostTypes.FAVORITE_POST,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final Post post = postRepository.findByTitleSlug(postKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));

    if (favoritePostForm.favorite()) {
      if (linkPersonPostRepository.getLinkPersonPostByPostAndPersonAndLinkType(post, person,
              LinkPersonPostType.follower)
          .isEmpty()) {
        linkPersonPostService.createPostLink(post, person, LinkPersonPostType.follower);
      }
    } else {
      linkPersonPostService.deleteLink(post, person, LinkPersonPostType.follower);
    }

    return conversionService.convert(post, PostResponse.class);
  }

  /**
   * Pins a post.
   *
   * @param postKey     The key of the post to pin.
   * @param pinPostForm The PinPost object containing the pin information.
   * @param person      The Person object representing the user pinning the post.
   * @return The PostResponse object for the pinned post.
   * @throws ResponseStatusException If the post is not found or the user does not have permission
   *                                 to pin the post.
   */
  public PostResponse pin(final String postKey, final PinPost pinPostForm, final Person person) {

    final Post post = postRepository.findByTitleSlug(postKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));

    if (!rolePermissionService.isPermitted(person, RolePermissionPostTypes.ADMIN_PIN_POST)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    post.setFeatured(pinPostForm.pin() != null ? pinPostForm.pin() : !post.isFeatured());
    postService.updatePost(post);

    return conversionService.convert(post, PostResponse.class);
  }

  /**
   * Pins a post in a community.
   *
   * @param postKey     The key of the post to pin.
   * @param pinPostForm The PinPost object containing the pin information.
   * @param person      The Person object representing the user pinning the post.
   * @return The PostResponse object for the pinned post.
   * @throws ResponseStatusException If the post is not found or the user does not have permission
   *                                 to pin the post.
   */
  public PostResponse pinCommunity(final String postKey, final PinPost pinPostForm,
      final Person person)
  {

    final Post post = postRepository.findByTitleSlug(postKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));

    if (!rolePermissionService.isPermitted(person, RolePermissionPostTypes.ADMIN_PIN_POST) && !(
        rolePermissionService.isPermitted(person, RolePermissionPostTypes.MODERATOR_PIN_POST)
            && !linkPersonCommunityService.hasAnyLink(person, post.getCommunity(),
            List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner)))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    post.setFeaturedInCommunity(pinPostForm.pin());
    postService.updatePost(post);

    return conversionService.convert(post, PostResponse.class);
  }

  /**
   * Removes a post from the system.
   *
   * @param postKey       The key of the post to be removed.
   * @param purgePostForm The PurgePost object containing the removal information.
   * @param person        The Person object representing the user removing the post.
   * @return The RequestResponse object indicating the result of the purge operation.
   * @throws ResponseStatusException If the post is not found, the user is unauthorized, or an error
   *                                 occurs during the removal process.
   */
  public RequestResponse purge(final String postKey, final PurgePost purgePostForm,
      final Person person)
  {

    final Post post = postRepository.findByTitleSlug(postKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post_not_found"));

    aclService.canPerson(person)
        .onCommunity(post.getCommunity())
        .performTheAction(RolePermissionPostTypes.PURGE_POST)
        .orThrowUnauthorized();

    // @todo: implement

    return RequestResponse.builder()
        .success(false)
        .error("not_implemented")
        .build();
  }
}