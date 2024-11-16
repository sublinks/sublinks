package com.sublinks.sublinksapi.api.sublinks.v1.languages.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.languages.models.LanguageResponse;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.language.entities.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("api/v1/languages")
@Tag(name = "Sublinks Languages", description = "Languages API")
@AllArgsConstructor
public class SublinksLanguageController extends AbstractSublinksApiController {

  private final LocalInstanceContext localInstanceContext;
  private final ConversionService conversionService;

  @Operation(summary = "Get a list of languagesKeys")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "List of languagesKeys",
          useReturnTypeSchema = true)})
  public List<LanguageResponse> index() {

    List<Language> languages = localInstanceContext.instance()
        .getLanguages();

    return languages.stream()
        .map(language -> conversionService.convert(language, LanguageResponse.class))
        .toList();

  }

  @Operation(summary = "Get a specific language")
  @GetMapping("/{id}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Language", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "404", description = "Language not found")})
  public LanguageResponse show(@PathVariable String id) {

    List<Language> languages = localInstanceContext.instance()
        .getLanguages();

    Language foundLanguage = languages.stream()
        .filter(language -> language.getId()
            .equals(Long.valueOf(id)))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "language_not_found"));

    return conversionService.convert(foundLanguage, LanguageResponse.class);
  }
}
