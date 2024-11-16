package com.sublinks.sublinksapi.api.sublinks.v1.person.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.common.models.RequestResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.moderation.BanPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.person.services.SublinksPersonService;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models.moderation.PurgePrivateMessage;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.services.SublinksPrivateMessageService;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPrivateMessageTypes;
import com.sublinks.sublinksapi.authorization.services.AclService;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/person/{key}/moderation")
@Tag(name = "Sublinks Person Moderation", description = "Person Moderation API")
@AllArgsConstructor
public class SublinksPersonModerationController extends AbstractSublinksApiController {

  private final SublinksPersonService sublinksPersonService;
  private final AclService aclService;
  private final SublinksPrivateMessageService sublinksPrivateMessageService;

  @Operation(summary = "Ban a person")
  @GetMapping("/ban")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PersonResponse ban(@PathVariable String key, @RequestBody @Valid BanPerson banPersonForm,
      final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    return sublinksPersonService.banPerson(banPersonForm, person);
  }


  @Operation(summary = "Delete/Purge an person ( as an admin )")
  @DeleteMapping("/purge")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse purge(@PathVariable String key) {
    // TODO: implement

    return RequestResponse.builder()
        .success(true)
        .build();
  }

  @Operation(summary = "Delete/Purge an person ( as an admin )")
  @DeleteMapping("/purge/privatemessages")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse purgeAll(@PathVariable String key,
      @RequestBody final PurgePrivateMessage purgePrivateMessageForm,
      final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    aclService.canPerson(person)
        .performTheAction(RolePermissionPrivateMessageTypes.PURGE_PRIVATE_MESSAGES)
        .orThrowUnauthorized();

    sublinksPrivateMessageService.purgeAllPrivateMessages(key, purgePrivateMessageForm, person);

    return RequestResponse.builder()
        .success(true)
        .build();
  }
}
