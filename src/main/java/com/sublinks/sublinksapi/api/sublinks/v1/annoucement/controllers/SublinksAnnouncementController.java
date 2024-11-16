package com.sublinks.sublinksapi.api.sublinks.v1.annoucement.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.AnnouncementResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.CreateAnnouncement;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.IndexAnnouncement;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.UpdateAnnouncement;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.services.SublinksAnnouncementService;
import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("api/v1/announcement")
@Tag(name = "Sublinks Announcement", description = "Announcement API")
@AllArgsConstructor
public class SublinksAnnouncementController extends AbstractSublinksApiController {

  private final SublinksAnnouncementService sublinksAnnouncementService;

  @Operation(summary = "Get a list of announcements")
  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<AnnouncementResponse> index(final IndexAnnouncement indexAnnouncementForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksAnnouncementService.index(indexAnnouncementForm, person.orElse(null));
  }

  @Operation(summary = "Get a specific announcement")
  @GetMapping("/{key}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public AnnouncementResponse show(@PathVariable String key,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    return sublinksAnnouncementService.show(Long.parseLong(key), person.orElse(null));
  }

  @Operation(summary = "Create a new announcement")
  @PostMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public AnnouncementResponse create(@RequestBody final CreateAnnouncement createAnnouncementForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksAnnouncementService.create(createAnnouncementForm, person);
  }

  @Operation(summary = "Update an announcement")
  @PostMapping("/{id}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public AnnouncementResponse update(@PathVariable final String id,
      @RequestBody final UpdateAnnouncement updateAnnouncementForm,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksAnnouncementService.update(Long.parseLong(id), updateAnnouncementForm, person);
  }

  @Operation(summary = "Delete an announcement")
  @DeleteMapping("/{id}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public AnnouncementResponse delete(@PathVariable final String id,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksAnnouncementService.delete(Long.parseLong(id), person);

  }
}
