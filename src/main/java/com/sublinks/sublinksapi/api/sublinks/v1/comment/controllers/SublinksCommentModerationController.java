package com.sublinks.sublinksapi.api.sublinks.v1.comment.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.CommentResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.Moderation.PinComment;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.Moderation.PurgeComment;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.Moderation.RemoveComment;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.services.SublinksCommentService;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.common.models.RequestResponse;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/comment/{key}/moderation")
@Tag(name = "Sublinks Comment Moderation", description = "Comment Moderation API")
public class SublinksCommentModerationController extends AbstractSublinksApiController {

  private final SublinksCommentService sublinksCommentService;

  @Operation(summary = "Remove a comment")
  @PostMapping("/remove")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommentResponse remove(@PathVariable final String key,
      @RequestBody @Valid final RemoveComment removeComment,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksCommentService.remove(key, removeComment, person);
  }

  @Operation(summary = "Purge a comment")
  @PostMapping("/purge")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse delete(@PathVariable final String key,
      @RequestBody @Valid final PurgeComment purgeCommentForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    // @todo: Implement purge

    return RequestResponse.builder()
        .success(false)
        .error("not_implemented")
        .build();
  }

  @Operation(summary = "Pin/Unpin a comment")
  @GetMapping("/pin")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommentResponse pin(@PathVariable final String key, final PinComment pinCommentForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksCommentService.pin(key, pinCommentForm, person);
  }
}
