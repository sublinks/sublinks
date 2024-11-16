package com.sublinks.sublinksapi.api.sublinks.v1.instance.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceConfigResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.UpdateInstanceConfig;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.service.SublinksInstanceService;
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

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/instance/{key}/config")
@Tag(name = "Sublinks Instance Config", description = "Instance Config API")
public class SublinksInstanceConfigController extends AbstractSublinksApiController {

  private final SublinksInstanceService sublinksInstanceService;

  @Operation(summary = "Get a specific instance config")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public InstanceConfigResponse show(@PathVariable String key,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksInstanceService.showConfig(key, person);
  }

  @Operation(summary = "Update an instance config")
  @PostMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public InstanceConfigResponse update(@PathVariable String key,
      @RequestBody @Valid UpdateInstanceConfig updateInstanceConfigForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksInstanceService.updateConfig(key, updateInstanceConfigForm, person);
  }
}
