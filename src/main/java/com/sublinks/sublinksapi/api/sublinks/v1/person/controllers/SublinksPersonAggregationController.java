package com.sublinks.sublinksapi.api.sublinks.v1.person.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonAggregateResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.services.SublinksPersonService;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPersonTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.repositories.PersonAggregateRepository;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("api/v1/person/{key}/aggregation")
@Tag(name = "Sublinks Person Aggegation", description = "Person Aggregation API")
@AllArgsConstructor
public class SublinksPersonAggregationController extends AbstractSublinksApiController {

  private final SublinksPersonService sublinksPersonService;
  private final ConversionService conversionService;
  private final RolePermissionService rolePermissionService;
  private final PersonAggregateRepository personAggregateRepository;
  private final PersonRepository personRepository;

  @Operation(summary = "Get a person's aggregation")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PersonAggregateResponse aggregate(@PathVariable final String key,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    rolePermissionService.isPermitted(person.orElse(null),
        RolePermissionPersonTypes.READ_PERSON_AGGREGATION,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    return conversionService.convert(sublinksPersonService.showAggregate(key, person.orElse(null)),
        PersonAggregateResponse.class);
  }
}
