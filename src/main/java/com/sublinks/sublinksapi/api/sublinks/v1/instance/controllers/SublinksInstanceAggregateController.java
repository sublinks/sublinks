package com.sublinks.sublinksapi.api.sublinks.v1.instance.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceAggregateResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.service.SublinksInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/instance/{key}/aggregate")
@Tag(name = "Sublinks Instance Aggregate", description = "Instance Aggretate API")
public class SublinksInstanceAggregateController extends AbstractSublinksApiController {

  private final SublinksInstanceService sublinksInstanceService;

  @Operation(summary = "Get a specific instance Aggregate")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public InstanceAggregateResponse show(@PathVariable String key) {

    return sublinksInstanceService.showAggregate(key);
  }
}
