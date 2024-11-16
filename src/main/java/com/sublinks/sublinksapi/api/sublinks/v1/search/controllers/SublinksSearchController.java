package com.sublinks.sublinksapi.api.sublinks.v1.search.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.search.models.Search;
import com.sublinks.sublinksapi.api.sublinks.v1.search.models.SearchResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.search.services.SublinksSearchService;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/search")
@Tag(name = "Sublinks Search", description = "Search API")
public class SublinksSearchController extends AbstractSublinksApiController {

  private final SublinksSearchService sublinksSearchService;

  @Operation(summary = "Get a list of privatemessages")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public SearchResponse index(@RequestBody Search search, final SublinksJwtPerson principal) {

    final Optional<Person> person = getOptionalPerson(principal);

    return sublinksSearchService.list(search, person);
  }
}
