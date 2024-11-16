package com.sublinks.sublinksapi.api.sublinks.v1.community.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.common.controllers.AbstractSublinksApiController;
import com.sublinks.sublinksapi.api.sublinks.v1.common.models.RequestResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.moderation.CommunityBanPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.moderation.CommunityModeratorResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.moderation.PurgeCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.moderation.RemoveCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.services.SublinksCommunityService;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonResponse;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionCommunityTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.community.repositories.CommunityRepository;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.person.services.LinkPersonCommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/community/{key}/moderation")
@Tag(name = "Sublinks Community Moderation", description = "Community Moderation API")
public class SublinksCommunityModerationController extends AbstractSublinksApiController {

  private final LinkPersonCommunityService linkPersonCommunityService;
  private final SublinksCommunityService sublinksCommunityService;
  private final CommunityRepository communityRepository;
  private final PersonRepository personRepository;
  private final ConversionService conversionService;
  private final RolePermissionService rolePermissionService;

  @Operation(summary = "Remove a community")
  @PostMapping("/remove")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public CommunityResponse remove(@PathVariable final String key,
      @RequestBody RemoveCommunity removeCommunityForm, SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    return sublinksCommunityService.remove(key, removeCommunityForm, person);
  }

  @Operation(summary = "Purge a community")
  @PostMapping("/purge")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public RequestResponse purge(@PathVariable final String key,
      @RequestBody @Valid PurgeCommunity purgeCommunityForm, SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    // @todo: Implement purge

    return RequestResponse.builder()
        .success(true)
        .build();
  }


  @Operation(summary = "Get moderators of the community")
  @GetMapping("/moderators")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<CommunityModeratorResponse> show(@PathVariable final String key,
      final SublinksJwtPerson sublinksJwtPerson)
  {

    Optional<Person> person = getOptionalPerson(sublinksJwtPerson);

    rolePermissionService.isPermitted(person.orElse(null),
        RolePermissionCommunityTypes.READ_COMMUNITY_MODERATORS, () -> {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
        });

    return sublinksCommunityService.getCommunityModerators(key, person.orElse(null))
        .stream()
        .map(communityModerator -> conversionService.convert(communityModerator,
            CommunityModeratorResponse.class))
        .toList();
  }

  @Operation(summary = "Add a moderator to the community")
  @PostMapping("/moderator/add/{personKey}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<CommunityModeratorResponse> add(@PathVariable final String key,
      @PathVariable final String personKey, final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    final Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    if (!(linkPersonCommunityService.hasAnyLinkOrAdmin(person, community,
        List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))
        && rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.MODERATOR_ADD_MODERATOR))
        && !rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.ADMIN_ADD_COMMUNITY_MODERATOR)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    final Person newModerator = personRepository.findOneByNameIgnoreCase(personKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    if (linkPersonCommunityService.hasAnyLinkOrAdmin(newModerator, community,
        List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "person_already_moderator");
    }

    linkPersonCommunityService.createLinkPersonCommunityLink(community, newModerator,
        LinkPersonCommunityType.moderator);

    return sublinksCommunityService.getCommunityModerators(key, person)
        .stream()
        .map(communityModerator -> conversionService.convert(communityModerator,
            CommunityModeratorResponse.class))
        .toList();
  }

  @Operation(summary = "Remove a moderator from the community")
  @PostMapping("/moderator/remove/{personKey}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<CommunityModeratorResponse> remove(@PathVariable final String key,
      @PathVariable final String personKey)
  {

    final Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    final Person person = personRepository.findOneByNameIgnoreCase(personKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    if (!linkPersonCommunityService.hasAnyLinkOrAdmin(person, community,
        List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    linkPersonCommunityService.deleteLink(community, person, LinkPersonCommunityType.moderator);

    return sublinksCommunityService.getCommunityModerators(key, person)
        .stream()
        .map(communityModerator -> conversionService.convert(communityModerator,
            CommunityModeratorResponse.class))
        .toList();
  }

  @Operation(summary = "Ban/Unban a user from a community")
  @PostMapping("/ban/{personKey}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public PersonResponse ban(@PathVariable final String key, @PathVariable final String personKey,
      CommunityBanPerson communityBanPersonForm, final SublinksJwtPerson sublinksJwtPerson)
  {

    final Person person = getPersonOrThrowUnauthorized(sublinksJwtPerson);

    final Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    if (!linkPersonCommunityService.hasAnyLinkOrAdmin(person, community,
        List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    final Person bannedPerson = sublinksCommunityService.banPerson(key, personKey, person,
        communityBanPersonForm);

    return conversionService.convert(bannedPerson, PersonResponse.class);
  }


  @Operation(summary = "Get banned users from a community")
  @GetMapping("/banned")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)})
  public List<PersonResponse> banned(@PathVariable final String key) {

    final Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    return linkPersonCommunityService.getLinkPersonCommunitiesByCommunityAndPersonAndLinkTypeIsIn(
            community, List.of(LinkPersonCommunityType.banned))
        .stream()
        .map(person -> conversionService.convert(person, PersonResponse.class))
        .toList();
  }
}
