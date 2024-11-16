package com.sublinks.sublinksapi.api.sublinks.v1.roles.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.common.models.RequestResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.models.CreateRole;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.models.IndexRole;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.models.RoleResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.models.UpdateRole;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.services.SublinksRoleService;
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
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/roles")
@Tag(name = "Sublinks Roles", description = "Roles API")
public class SublinksRolesController extends AbstractSublinksApiController {

  private final SublinksRoleService sublinksRoleService;

  @Operation(summary = "Get a list of roles")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<RoleResponse> index(@Valid final IndexRole indexRoleForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksRoleService.indexRole(indexRoleForm, person.orElse(null));
  }

  @Operation(summary = "Get a specific role")
  @GetMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RoleResponse show(@PathVariable String key, final SublinksJwtPerson sublinksJwtPerson) {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksRoleService.show(key, person.orElse(null));
  }

  @Operation(summary = "Create a new Role")
  @PostMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RoleResponse create(@Valid @RequestBody CreateRole createRoleForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksRoleService.create(createRoleForm, person);
  }

  @Operation(summary = "Update an Role")
  @PostMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RoleResponse update(@PathVariable String key,
      @Valid @RequestBody UpdateRole updateRoleForm, final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksRoleService.update(key, updateRoleForm, person);
  }

  @Operation(summary = "Delete an Role")
  @DeleteMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse delete(@PathVariable String key, final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    sublinksRoleService.delete(key, person);

    return RequestResponse.builder()
        .success(true)
        .build();
  }
}
