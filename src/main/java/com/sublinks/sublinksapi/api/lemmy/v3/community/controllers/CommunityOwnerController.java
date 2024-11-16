package com.sublinks.sublinksapi.api.lemmy.v3.community.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CreateCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.EditCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.services.LemmyCommunityService;
import com.sublinks.sublinksapi.api.lemmy.v3.errorhandler.ApiError;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionCommunityTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.community.repositories.CommunityRepository;
import com.sublinks.sublinksapi.community.services.CommunityService;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.language.entities.Language;
import com.sublinks.sublinksapi.person.entities.LinkPersonCommunity;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.repositories.LinkPersonCommunityRepository;
import com.sublinks.sublinksapi.slurfilter.exceptions.SlurFilterBlockedException;
import com.sublinks.sublinksapi.slurfilter.exceptions.SlurFilterReportException;
import com.sublinks.sublinksapi.slurfilter.services.SlurFilterService;
import com.sublinks.sublinksapi.utils.SlugUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v3/community")
@Tag(name = "Community")
public class CommunityOwnerController extends AbstractLemmyApiController {

  private final LocalInstanceContext localInstanceContext;
  private final LinkPersonCommunityRepository linkPersonCommunityRepository;
  private final CommunityService communityService;
  private final RolePermissionService rolePermissionService;
  private final LemmyCommunityService lemmyCommunityService;
  private final SlugUtil slugUtil;
  private final CommunityRepository communityRepository;
  private final SlurFilterService slurFilterService;

  @Operation(summary = "Create a new community.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = CommunityResponse.class))})})
  @PostMapping
  @Transactional
  public CommunityResponse create(@Valid @RequestBody final CreateCommunity createCommunityForm,
      JwtPerson principal) {

    Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionCommunityTypes.CREATE_COMMUNITY,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final List<Language> languages = new ArrayList<>();
    if (createCommunityForm.discussion_languages() != null) {
      for (String languageCode : createCommunityForm.discussion_languages()) {
        final Optional<Language> language = localInstanceContext.languageRepository().findById(
            Long.valueOf(languageCode));
        language.ifPresent(languages::add);
      }
    }

    Community.CommunityBuilder communityBuilder = Community.builder().instance(
        localInstanceContext.instance()).title(createCommunityForm.title()).titleSlug(
        slugUtil.stringToSlug(createCommunityForm.name())).description(
        createCommunityForm.description())
        .isPostingRestrictedToMods(createCommunityForm.posting_restricted_to_mods() != null
            && createCommunityForm.posting_restricted_to_mods()).isNsfw(
        createCommunityForm.nsfw() != null && createCommunityForm.nsfw()).iconImageUrl(
        createCommunityForm.icon()).bannerImageUrl(createCommunityForm.banner()).languages(
        languages);

    try {
      communityBuilder.title(slurFilterService.censorText(createCommunityForm.title()));
    } catch (SlurFilterReportException e) {
      // Do nothing as we cant report communities
      communityBuilder.title(createCommunityForm.title());
    } catch (SlurFilterBlockedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "community_blocked_by_slur_filter");
    }

    try {
      communityBuilder.description(slurFilterService.censorText(createCommunityForm.description()));
    } catch (SlurFilterReportException e) {
      // Do nothing as we cant report communities
      communityBuilder.description(createCommunityForm.description());
    } catch (SlurFilterBlockedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "community_blocked_by_slur_filter");
    }

    // Prevent users from creating duplicate communities
    final Optional<Community> oldCommunity = Optional.ofNullable(
        communityRepository.findCommunityByIsLocalTrueAndTitleSlug(
            slugUtil.stringToSlug(createCommunityForm.name())));
    if (oldCommunity.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "community_already_exists");
    }

    try {
      String censorText = slurFilterService.censorText(createCommunityForm.name());
      // We dont want censored slugs.... like c/#####communitytest
      if (!Objects.equals(censorText, createCommunityForm.name())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "community_blocked_by_slur_filter");
      }
      communityBuilder.titleSlug(slugUtil.stringToSlug(createCommunityForm.name()));
    } catch (SlurFilterReportException | SlurFilterBlockedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "community_blocked_by_slur_filter");
    }

    Community community = communityBuilder.build();

    final Set<LinkPersonCommunity> linkPersonCommunities = new LinkedHashSet<>();
    linkPersonCommunities.add(LinkPersonCommunity.builder()
        .community(community)
        .person(person)
        .linkType(LinkPersonCommunityType.owner)
        .build());
    linkPersonCommunities.add(LinkPersonCommunity.builder()
        .community(community)
        .person(person)
        .linkType(LinkPersonCommunityType.follower)
        .build());

    communityService.createCommunity(community);

    linkPersonCommunityRepository.saveAllAndFlush(linkPersonCommunities);

    return lemmyCommunityService.createCommunityResponse(community, person);
  }

  @Operation(summary = "Edit a community.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = CommunityResponse.class))}),
      @ApiResponse(responseCode = "404",
          description = "Community Not Found",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiError.class))})})
  @PutMapping
  CommunityResponse update(@Valid final @RequestBody EditCommunity editCommunityForm,
      final JwtPerson principal) {

    Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionCommunityTypes.UPDATE_COMMUNITY,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    Community community = communityRepository.findById(editCommunityForm.community_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

    community.setIconImageUrl(editCommunityForm.icon());
    community.setBannerImageUrl(editCommunityForm.banner());
    community.setNsfw(editCommunityForm.nsfw());
    community.setPostingRestrictedToMods(editCommunityForm.posting_restricted_to_mods());

    try {
      community.setTitle(slurFilterService.censorText(editCommunityForm.title()));
    } catch (SlurFilterReportException e) {
      // Do nothing as we cant report communities
      community.setTitle(editCommunityForm.title());
    } catch (SlurFilterBlockedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "community_blocked_by_slur_filter");
    }

    try {
      community.setDescription(slurFilterService.censorText(editCommunityForm.description()));
    } catch (SlurFilterReportException e) {
      // Do nothing as we cant report communities
      community.setDescription(editCommunityForm.description());
    } catch (SlurFilterBlockedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "community_blocked_by_slur_filter");
    }

    final List<Language> languages = new ArrayList<>();
    for (String languageCode : editCommunityForm.discussion_languages()) {
      final Optional<Language> language = localInstanceContext.languageRepository().findById(
          Long.valueOf(languageCode));
      language.ifPresent(languages::add);
    }

    community.setLanguages(languages);

    communityService.updateCommunity(community);

    return lemmyCommunityService.createCommunityResponse(community, person);
  }
}
