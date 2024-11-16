package com.sublinks.sublinksapi.api.sublinks.v1.community.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CommunityAggregateResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.community.services.SublinksCommunityService;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.community.repositories.CommunityAggregateRepository;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/community/{key}/aggregate")
@Tag(name = "Sublinks Community Aggregation", description = "Community Aggregation API")
public class SublinksCommunityAggregationController extends AbstractSublinksApiController {

  private final CommunityAggregateRepository communityAggregateRepository;
  private final ConversionService conversionService;
  private final RolePermissionService rolePermissionService;
  private final SublinksCommunityService sublinksCommunityService;

  @Operation(summary = "Get a community aggregate")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommunityAggregateResponse show(@PathVariable final String key,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksCommunityService.showAggregate(key, person.orElse(null));
  }
}
