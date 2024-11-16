package com.sublinks.sublinksapi.api.sublinks.v1.post.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.common.models.RequestResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.PostResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation.PinPost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation.PurgePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation.RemovePost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.services.SublinksPostService;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/post/{key}/moderation")
@Tag(name = "Sublinks Post Moderation", description = "Post Moderation API")
@AllArgsConstructor
public class SublinksPostModerationController extends AbstractSublinksApiController {

  private final SublinksPostService sublinksPostService;

  @Operation(summary = "Remove a post")
  @PostMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public void remove(@PathVariable final String key, @RequestBody final RemovePost removePostForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    sublinksPostService.remove(key, removePostForm, person);
  }

  @Operation(summary = "Pin a post")
  @PostMapping("/pin")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PostResponse pin(@PathVariable final String key, @RequestBody final PinPost pinPostForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksPostService.pin(key, pinPostForm, person);
  }

  @Operation(summary = "Pin a post in a community")
  @PostMapping("/pin/community")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PostResponse pinInCommunity(@PathVariable final String key,
      @RequestBody final PinPost pinPostForm, final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksPostService.pinCommunity(key, pinPostForm, person);
  }

  @Operation(summary = "Purge a post")
  @PostMapping("/purge")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse delete(@PathVariable final String key,
      @RequestBody final PurgePost purgePostForm, final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksPostService.purge(key, purgePostForm != null ? purgePostForm
        : PurgePost.builder()
            .build(), person);

  }
}
