package com.sublinks.sublinksapi.api.sublinks.v1.community.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CreateCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.DeleteCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.IndexCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.UpdateCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.services.SublinksCommunityService;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/community")
@Tag(name = "Sublinks Community", description = "Community API")
public class SublinksCommunityController extends AbstractSublinksApiController {

  private final SublinksCommunityService sublinksCommunityService;

  @Operation(summary = "Get a list of communities")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<CommunityResponse> index(
      @RequestParam(required = false) final Optional<IndexCommunity> indexCommunityParam,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksCommunityService.index(indexCommunityParam.orElse(IndexCommunity.builder()
        .build()), person.orElse(null));

  }

  @Operation(summary = "Get a specific community")
  @GetMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommunityResponse show(@PathVariable final String key,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksCommunityService.show(key, person.orElse(null));
  }

  @Operation(summary = "Create a new community")
  @PostMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommunityResponse create(@RequestBody @Valid final CreateCommunity createCommunity,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksCommunityService.createCommunity(createCommunity, person);
  }

  @Operation(summary = "Update an community")
  @PostMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommunityResponse update(@PathVariable final String key,
      @RequestBody @Valid UpdateCommunity updateCommunityForm, final SublinksJwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    return sublinksCommunityService.updateCommunity(key, updateCommunityForm, person);
  }

  @Operation(summary = "Delete a community")
  @DeleteMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommunityResponse delete(@PathVariable final String key,
      final DeleteCommunity deleteCommunityParam, final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksCommunityService.delete(key, deleteCommunityParam, person);
  }
}
