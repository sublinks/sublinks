package com.sublinks.sublinksapi.api.sublinks.v1.post.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.common.models.RequestResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.CreatePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.DeletePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.IndexPost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.PostResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.UpdatePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation.FavoritePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.services.SublinksPostService;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/post")
@Tag(name = "Sublinks Post", description = "Post API")
@AllArgsConstructor
public class SublinksPostController extends AbstractSublinksApiController {

  private final SublinksPostService sublinksPostService;

  @Operation(summary = "Get a list of posts")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<PostResponse> index(final Optional<IndexPost> indexPost,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksPostService.index(indexPost.orElse(IndexPost.builder()
        .build()), person.orElse(null));
  }

  @Operation(summary = "Get a specific post")
  @GetMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PostResponse show(@PathVariable String key, final SublinksJwtPerson sublinksJwtPerson) {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksPostService.show(key, person.orElse(null));
  }

  @Operation(summary = "Create a new post")
  @PostMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public void create(final CreatePost createPost, final SublinksJwtPerson sublinksJwtPerson) {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    sublinksPostService.create(createPost, person);
  }

  @Operation(summary = "Update an post")
  @PostMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PostResponse update(final UpdatePost updatePostForm, @PathVariable final String key,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksPostService.update(key, updatePostForm, person);
  }

  @Operation(summary = "Delete an post")
  @DeleteMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse delete(@PathVariable final String key, final DeletePost deletePostParam,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    sublinksPostService.delete(key, deletePostParam, person);

    return RequestResponse.builder()
        .success(true)
        .build();
  }

  @Operation(summary = "Favorite a post")
  @PostMapping("{key}/favorite")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PostResponse favorite(@PathVariable final String key,
      @RequestBody final FavoritePost favoritePostForm, final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksPostService.favorite(key, favoritePostForm, person);
  }

}
