package com.sublinks.sublinksapi.api.lemmy.v3.search.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.comment.models.CommentView;
import com.sublinks.sublinksapi.api.lemmy.v3.comment.services.LemmyCommentService;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.community.services.LemmyCommunityService;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.SearchType;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.PostView;
import com.sublinks.sublinksapi.api.lemmy.v3.post.services.LemmyPostService;
import com.sublinks.sublinksapi.api.lemmy.v3.search.models.Search;
import com.sublinks.sublinksapi.api.lemmy.v3.search.models.SearchResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.PersonView;
import com.sublinks.sublinksapi.api.lemmy.v3.user.services.LemmyPersonService;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInstanceTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.comment.entities.Comment;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.post.entities.Post;
import com.sublinks.sublinksapi.search.services.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v3/search")
@Tag(name = "Miscellaneous")
public class SearchController extends AbstractLemmyApiController {

  private final SearchService searchService;
  private final LemmyCommunityService lemmyCommunityService;
  private final LemmyCommentService lemmyCommentService;
  private final LemmyPostService lemmyPostService;
  private final LemmyPersonService lemmyPersonService;
  private final RolePermissionService rolePermissionService;

  @Operation(summary = "Search lemmy.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = SearchResponse.class))}),
      @ApiResponse(responseCode = "400",
          description = "Gets returned if type_ is null",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ResponseStatusException.class))}

      )})
  @GetMapping
  SearchResponse search(@Valid final Search searchForm, final JwtPerson JwtPerson) {

    final Optional<Person> person = getOptionalPerson(JwtPerson);

    rolePermissionService.isPermitted(person.orElse(null),
        RolePermissionInstanceTypes.INSTANCE_SEARCH,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final SearchResponse.SearchResponseBuilder responseBuilder = SearchResponse.builder();

    final int page = (searchForm.page() == null ? 0 : Integer.parseInt(searchForm.page())) - 1;
    final int pLimit = Integer.parseInt(searchForm.limit());
    final int limit = Math.min(pLimit, 20);
    if (searchForm.type_() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type_ is required");
    }

    Sort sort = switch (searchForm.sort()) {
      case "New" -> Sort.by("createdAt")
          .descending();
      case "Old" -> Sort.by("createdAt")
          .ascending();
      default -> Sort.unsorted();
    };

    final boolean isAll = searchForm.type_() == SearchType.All;

    final List<CommentView> commentViewList = new ArrayList<>();
    final List<PostView> postViewList = new ArrayList<>();
    final List<CommunityView> communityViewList = new ArrayList<>();
    final List<PersonView> peopleViewList = new ArrayList<>();

    if (searchForm.type_() == SearchType.Comments || isAll) {
      List<Comment> comments = searchService.searchComments(searchForm.q(), page, limit, sort)
          .getContent();
      commentViewList.addAll(comments.stream()
          .map(lemmyCommentService::createCommentView)
          .toList());

    }

    if (searchForm.type_() == SearchType.Posts || isAll) {
      List<Post> posts = searchService.searchPost(searchForm.q(), page, limit, sort)
          .getContent();
      postViewList.addAll(posts.stream()
          .map(lemmyPostService::postViewFromPost)
          .toList());
    }

    if (searchForm.type_() == SearchType.Communities || isAll) {
      List<Community> communities = searchService.searchCommunity(searchForm.q(), page, limit, sort)
          .getContent();
      communityViewList.addAll(communities.stream()
          .map(lemmyCommunityService::communityViewFromCommunity)
          .toList());

    }

    if (searchForm.type_() == SearchType.Users || isAll) {
      List<Person> people = searchService.searchPerson(searchForm.q(), page, limit, sort)
          .getContent();
      peopleViewList.addAll(people.stream()
          .map(lemmyPersonService::getPersonView)
          .toList());

    }

    if (searchForm.type_() == SearchType.Url || isAll) {
      List<Post> posts = searchService.searchPostByUrl(searchForm.q(), page, limit, sort)
          .getContent();
      postViewList.addAll(posts.stream()
          .map(lemmyPostService::postViewFromPost)
          .toList());
    }

    responseBuilder.comments(commentViewList);
    responseBuilder.posts(postViewList);
    responseBuilder.communities(communityViewList);
    responseBuilder.users(peopleViewList);

    return responseBuilder.build();
  }
}
