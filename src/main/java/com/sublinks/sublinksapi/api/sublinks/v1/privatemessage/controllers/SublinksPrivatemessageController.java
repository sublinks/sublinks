package com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models.CreatePrivateMessage;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models.DeletePrivateMessage;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models.IndexPrivateMessages;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models.PrivateMessageResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models.UpdatePrivateMessage;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.services.SublinksPrivateMessageService;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/privatemessage")
@Tag(name = "Sublinks Private Messages", description = "Private Messages API")
public class SublinksPrivatemessageController extends AbstractSublinksApiController {

  private final SublinksPrivateMessageService sublinksPrivateMessageService;

  @Operation(summary = "Get a list of privatemessages")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<PrivateMessageResponse> index(final IndexPrivateMessages indexPrivateMessagesForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksPrivateMessageService.index(indexPrivateMessagesForm, person);
  }

  @Operation(summary = "Get a specific privatemessage")
  @GetMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PrivateMessageResponse show(@PathVariable String key,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksPrivateMessageService.show(key, person);
  }

  @Operation(summary = "Create a new privatemessage")
  @PostMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PrivateMessageResponse create(final CreatePrivateMessage createPrivateMessageForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksPrivateMessageService.create(createPrivateMessageForm, person);
  }

  @Operation(summary = "Update an privatemessage")
  @PostMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PrivateMessageResponse update(@PathVariable String key,
      final UpdatePrivateMessage updatePrivateMessageForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowBadRequest(sublinksJwtPerson);

    return sublinksPrivateMessageService.update(updatePrivateMessageForm, person);
  }

  @Operation(summary = "Delete an privatemessage")
  @DeleteMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PrivateMessageResponse delete(@PathVariable String key,
      final DeletePrivateMessage deletePrivateMessageForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowBadRequest(sublinksJwtPerson);

    return sublinksPrivateMessageService.delete(key, deletePrivateMessageForm, person);
  }
}
