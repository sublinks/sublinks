package com.sublinks.sublinksapi.api.sublinks.v1.person.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.common.models.RequestResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonSessionDataResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.services.SublinksPersonService;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.PersonKeyUtils;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/session/")
@Tag(name = "Sublinks  Person Session", description = "Person Session API")
@AllArgsConstructor
public class SublinksPersonSessionController extends AbstractSublinksApiController {

  private final SublinksPersonService sublinksPersonService;
  private final PersonKeyUtils personKeyUtils;

  @Operation(summary = "Get metadata for a person ( requires permission to view other peoples sessions )")
  @GetMapping("/person/{personKey}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PersonSessionDataResponse getMetaData(@PathVariable String personKey,
      final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    return sublinksPersonService.getMetaData(personKey, person);
  }

  @Operation(summary = "Get metadata for a person ( requires permission to view other peoples sessions )")
  @GetMapping("/person")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PersonSessionDataResponse getMetaData(final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    return sublinksPersonService.getMetaData(personKeyUtils.getPersonKey(person), person);
  }

  @GetMapping("/data/{sessionKey}")
  @Operation(summary = "Get one metadata ( requires permission to view other peoples sessions )")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PersonSessionDataResponse getOneMetaData(@PathVariable String sessionKey,
      final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    return sublinksPersonService.getOneMetaData(sessionKey, person);
  }

  @Operation(summary = "Invalidate one metadata for a person ( requires permission to invalidate other peoples metadata )")
  @DeleteMapping("/person/invalidate/{sessionKey}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse invalidateOneMetaData(@PathVariable String sessionKey,
      final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    sublinksPersonService.invalidateUserData(sessionKey, person);

    return RequestResponse.builder()
        .success(true)
        .build();
  }

  @Operation(summary = "Invalidate all metadata for a person ( requires permission to invalidate other peoples metadata )")
  @DeleteMapping("/person/{personKey}/invalidate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse invalidateMetaData(@PathVariable String personKey,
      final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    sublinksPersonService.invalidateAllUserData(personKey, person);

    return RequestResponse.builder()
        .success(true)
        .build();
  }

  @Operation(summary = "Deletes all metadata for a person ( requires permission to delete other peoples metadata )")
  @DeleteMapping("/person/{personKey}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse deleteMetaData(@PathVariable String personKey,
      final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    sublinksPersonService.deleteAllUserData(personKey, person);

    return RequestResponse.builder()
        .success(true)
        .build();
  }

  @Operation(summary = "Deletes one metadata for a person ( requires permission to delete other peoples metadata )")
  @DeleteMapping("/data/{sessionKey}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse deleteOneMetaData(@PathVariable String sessionKey,
      final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    sublinksPersonService.deleteUserData(sessionKey, person);

    return RequestResponse.builder()
        .success(true)
        .build();
  }

  @Operation(summary = "Invalidates your current session")
  @DeleteMapping("/invalidate")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse invalidateCurrentSession(final SublinksJwtPerson jwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(jwtPerson);

    sublinksPersonService.invalidateUserDataByToken(jwtPerson.getToken(), person);

    return RequestResponse.builder()
        .success(true)
        .build();
  }
}
