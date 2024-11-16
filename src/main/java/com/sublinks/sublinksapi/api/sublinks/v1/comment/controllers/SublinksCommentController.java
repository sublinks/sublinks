package com.sublinks.sublinksapi.api.sublinks.v1.comment.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.CommentResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.CreateComment;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.DeleteComment;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.IndexComment;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.UpdateComment;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.services.SublinksCommentService;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
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
@AllArgsConstructor
@RequestMapping("api/v1/comment")
@Tag(name = "Sublinks Comment", description = "Comment API")
public class SublinksCommentController extends AbstractSublinksApiController {

  private final SublinksCommentService sublinksCommentService;


  @Operation(summary = "Get a list of comments")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<CommentResponse> index(IndexComment indexCommentParam,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksCommentService.index(indexCommentParam != null ? indexCommentParam
        : IndexComment.builder()
            .build(), person.orElse(null));
  }

  @Operation(summary = "Get a specific comment")
  @GetMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommentResponse show(@PathVariable final String key,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksCommentService.show(key, person.orElse(null));
  }

  @Operation(summary = "Create a new comment")
  @PostMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommentResponse create(final CreateComment createCommentForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksCommentService.createComment(createCommentForm, person);
  }

  @Operation(summary = "Update an comment")
  @PostMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommentResponse update(@PathVariable String key, final UpdateComment createCommentForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksCommentService.updateComment(key, createCommentForm, person);
  }

  @Operation(summary = "Delete an comment")
  @DeleteMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommentResponse delete(@PathVariable String key, final DeleteComment deleteCommentForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksCommentService.delete(key, deleteCommentForm, person);
  }
}
