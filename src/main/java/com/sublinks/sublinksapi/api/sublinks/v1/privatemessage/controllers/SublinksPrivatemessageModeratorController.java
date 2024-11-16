package com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.common.models.RequestResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models.moderation.PurgePrivateMessage;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.services.SublinksPrivateMessageService;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/privatemessage/{key}/")
@Tag(name = "Sublinks Private Messages Moderation", description = "Private Messages Moderation API")
public class SublinksPrivatemessageModeratorController extends AbstractSublinksApiController {

  private final SublinksPrivateMessageService sublinksPrivateMessageService;

  @Operation(summary = "Purge an private message")
  @DeleteMapping("/purge")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse purge(@PathVariable String key,
      final PurgePrivateMessage purgePrivateMessageParam, final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksPrivateMessageService.purgePrivateMessage(key, purgePrivateMessageParam, person);
  }
}
