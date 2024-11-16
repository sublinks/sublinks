package com.sublinks.sublinksapi.api.sublinks.v1.instance.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.IndexInstance;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.service.SublinksInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/instance")
@Tag(name = "Sublinks Instance", description = "Instance API")
public class SublinksInstanceController extends AbstractSublinksApiController {

  private final SublinksInstanceService sublinksInstanceService;

  @Operation(summary = "Get a list of instances")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<InstanceResponse> index(final IndexInstance indexInstance)
  {

    return sublinksInstanceService.index(indexInstance == null ? IndexInstance.builder()
        .build() : indexInstance);
  }

  @Operation(summary = "Get a specific instance")
  @GetMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public InstanceResponse show(@PathVariable String key) {

    return sublinksInstanceService.show(key);
  }
}
