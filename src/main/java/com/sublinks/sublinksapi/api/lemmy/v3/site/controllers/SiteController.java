package com.sublinks.sublinksapi.api.lemmy.v3.site.controllers;

import com.sublinks.sublinksapi.announcement.entities.Announcement;
import com.sublinks.sublinksapi.announcement.repositories.AnnouncementRepository;
import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.BlockInstance;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.BlockInstanceResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.CreateSite;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.EditSite;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.GetSiteResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.SiteResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.Tagline;
import com.sublinks.sublinksapi.api.lemmy.v3.site.services.LemmySiteService;
import com.sublinks.sublinksapi.api.lemmy.v3.site.services.MyUserInfoService;
import com.sublinks.sublinksapi.api.lemmy.v3.user.services.LemmyPersonService;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInstanceTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.authorization.services.RoleService;
import com.sublinks.sublinksapi.instance.entities.Instance;
import com.sublinks.sublinksapi.instance.entities.InstanceBlock;
import com.sublinks.sublinksapi.instance.entities.InstanceConfig;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.instance.repositories.InstanceBlockRepository;
import com.sublinks.sublinksapi.instance.repositories.InstanceRepository;
import com.sublinks.sublinksapi.instance.services.InstanceConfigService;
import com.sublinks.sublinksapi.instance.services.InstanceService;
import com.sublinks.sublinksapi.language.services.LanguageService;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.slurfilter.services.SlurFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * This class represents a controller for managing site-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v3/site")
@Tag(name = "Site")
public class SiteController extends AbstractLemmyApiController {

  private final LocalInstanceContext localInstanceContext;
  private final LemmySiteService lemmySiteService;
  private final InstanceService instanceService;
  private final LanguageService languageService;
  private final InstanceRepository instanceRepository;
  private final InstanceBlockRepository instanceBlockRepository;
  private final MyUserInfoService myUserInfoService;
  private final RolePermissionService rolePermissionService;
  private final InstanceConfigService instanceConfigService;
  private final SlurFilterService slurFilterService;
  private final AnnouncementRepository announcementRepository;
  private final ConversionService conversionService;
  private final LemmyPersonService lemmyPersonService;
  private final RoleService roleService;

  /**
   * Retrieves the site data along with the user's data.
   *
   * @param principal The authenticated user token.
   * @return The GetSiteResponse containing the site data and user data.
   */
  @Operation(summary = "Gets the site, and your user data.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetSiteResponse.class))})})
  @GetMapping
  public GetSiteResponse getSite(final JwtPerson principal) {

    GetSiteResponse.GetSiteResponseBuilder builder = GetSiteResponse.builder()
        .version("0.19.3") // @todo grab this from config?
        .taglines(announcementRepository.findAll().stream()
            .map(tagline -> conversionService.convert(tagline, Tagline.class)).toList())
        .site_view(lemmySiteService.getSiteView())
        .discussion_languages(languageService.instanceLanguageIds(localInstanceContext.instance()))
        .all_languages(lemmySiteService.allLanguages(localInstanceContext.languageRepository()))
        .custom_emojis(lemmySiteService.customEmojis()).admins(
            roleService.getAdmins().stream().map(lemmyPersonService::getPersonView)
                .toList());

    getOptionalPerson(principal).ifPresent(
        (person -> builder.my_user(myUserInfoService.getMyUserInfo(person))));

    return builder.build();
  }

  /**
   * Creates a site with the given parameters.
   *
   * @param createSiteForm The form containing the data for creating the site.
   * @param principal The authenticated user token.
   * @return The SiteResponse containing the created site data.
   */
  @Operation(summary = "Create your site.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SiteResponse.class))})})
  @PostMapping
  @Transactional
  public SiteResponse createSite(@Valid @RequestBody final CreateSite createSiteForm,
      final JwtPerson principal) {

    if (!localInstanceContext.instance().getDomain().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    final Person person = getPersonOrThrowUnauthorized(principal);
    RolePermissionService.isAdminElseThrow(person,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    final Instance instance = localInstanceContext.instance();
    instance.setName(createSiteForm.name());
    instance.setDomain(localInstanceContext.settings().getBaseUrl());
    instance.setActivityPubId(localInstanceContext.settings().getBaseUrl());
    instance.setSoftware("sublinks");
    instance.setVersion("0.1.0");
    instance.setDescription(createSiteForm.description());
    instance.setSidebar(createSiteForm.sidebar());
    instance.setLanguages(
        languageService.languageIdsToEntity(createSiteForm.discussion_languages()));
    instance.setBannerUrl(createSiteForm.banner());
    instance.setIconUrl(createSiteForm.icon());
    instanceService.createInstanceAndFlush(instance);

    final InstanceConfig.InstanceConfigBuilder config = InstanceConfig.builder().instance(instance);
    config.registrationMode(createSiteForm.registration_mode());
    config.registrationQuestion(createSiteForm.application_question());

    config.actorNameMaxLength(createSiteForm.actor_name_max_length());
    config.captchaDifficulty(createSiteForm.captcha_difficulty());
    config.captchaEnabled(createSiteForm.captcha_enabled());
    config.communityCreationAdminOnly(createSiteForm.community_creation_admin_only());
    config.enableDownvotes(createSiteForm.enable_downvotes());
    config.enableNsfw(createSiteForm.enable_nsfw());
    config.federationEnabled(createSiteForm.federation_enabled());
    config.hideModlogModNames(createSiteForm.hide_modlog_mod_names());
    config.privateInstance(createSiteForm.private_instance());
    config.requireEmailVerification(createSiteForm.require_email_verification());
    config.applicationEmailAdmins(createSiteForm.application_email_admins());
    config.defaultTheme(createSiteForm.default_theme());
    config.defaultPostListingType(createSiteForm.default_post_listing_type());
    config.legalInformation(createSiteForm.legal_information());
    config.captchaDifficulty(createSiteForm.captcha_difficulty());
    config.reportEmailAdmins(false);

    final InstanceConfig instanceConfig = config.build();
    instanceConfigService.createInstanceConfig(instanceConfig);
    slurFilterService.updateOrCreateLemmySlur(createSiteForm.slur_filter_regex());

    instance.setInstanceConfig(instanceConfig);
    instanceService.updateInstance(instance);

    return SiteResponse.builder()
        .site_view(lemmySiteService.getSiteView())
        .tag_lines(announcementRepository.findAll()
            .stream()
            .map((x) -> conversionService.convert(x, Tagline.class))
            .toList())
        .build();
  }

  /**
   * Updates the site settings with the given form data.
   *
   * @param editSiteForm The form containing the updated site settings.
   * @param principal The authenticated user token.
   * @return The SiteResponse containing the updated site data.
   */
  @Operation(summary = "Edit your site.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SiteResponse.class))})})
  @PutMapping
  @Transactional
  public SiteResponse updateSite(@Valid @RequestBody final EditSite editSiteForm,
      final JwtPerson principal) {

    // @todo: Check permission on federation change

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        RolePermissionInstanceTypes.INSTANCE_UPDATE_SETTINGS,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    if (editSiteForm.taglines().isPresent()) {
      announcementRepository.deleteAll();
      editSiteForm.taglines()
          .get()
          .forEach(tagline -> announcementRepository.save(Announcement.builder()
              .content(tagline)
              .localSiteId(localInstanceContext.instance().getId())
              .build()));
      return SiteResponse.builder()
          .site_view(lemmySiteService.getSiteView())
          .tag_lines(new ArrayList<>())
          .build();
    }

    final Instance instance = localInstanceContext.instance();
    instance.setName(editSiteForm.name());
    instance.setDescription(editSiteForm.description());
    instance.setSidebar(editSiteForm.sidebar());
    instance.setLanguages(languageService.languageIdsToEntity(editSiteForm.discussion_languages()));
    instance.setBannerUrl(editSiteForm.banner());
    instance.setIconUrl(editSiteForm.icon());
    instanceService.updateInstance(instance);

    InstanceConfig config = instance.getInstanceConfig();

    if (config == null) {
      config = InstanceConfig.builder().build();
    }

    config.setActorNameMaxLength(editSiteForm.actor_name_max_length());
    config.setCaptchaDifficulty(editSiteForm.captcha_difficulty());
    config.setCaptchaEnabled(editSiteForm.captcha_enabled());
    config.setCommunityCreationAdminOnly(editSiteForm.community_creation_admin_only());
    config.setEnableDownvotes(editSiteForm.enable_downvotes());
    config.setEnableNsfw(editSiteForm.enable_nsfw());
    config.setFederationEnabled(editSiteForm.federation_enabled());
    config.setHideModlogModNames(editSiteForm.hide_modlog_mod_names());
    config.setPrivateInstance(editSiteForm.private_instance());
    config.setRequireEmailVerification(editSiteForm.require_email_verification());
    config.setApplicationEmailAdmins(editSiteForm.application_email_admins());
    config.setDefaultTheme(editSiteForm.default_theme());
    config.setDefaultPostListingType(editSiteForm.default_post_listing_type());
    config.setLegalInformation(editSiteForm.legal_information());
    config.setReportEmailAdmins(editSiteForm.application_email_admins());

    config.setRegistrationMode(editSiteForm.registration_mode());
    config.setRegistrationQuestion(editSiteForm.application_question());

    instanceConfigService.updateInstanceConfig(config);

    slurFilterService.updateOrCreateLemmySlur(editSiteForm.slur_filter_regex());

    return SiteResponse.builder()
        .site_view(lemmySiteService.getSiteView())
        .tag_lines(new ArrayList<>())
        .build();
  }

  /**
   * Blocks an instance.
   *
   * @param blockInstanceForm The form containing the instance ID and block flag.
   * @param principal The authenticated user token.
   * @return The BlockInstanceResponse indicating if the instance was successfully blocked.
   */
  @Operation(summary = "Block an instance.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BlockInstanceResponse.class))})})
  @PostMapping("/block")
  @Transactional
  public BlockInstanceResponse blockInstance(
      @Valid @RequestBody final BlockInstance blockInstanceForm, final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        RolePermissionInstanceTypes.INSTANCE_DEFEDERATE_INSTANCE,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Optional<Instance> instance = instanceRepository.findById(
        (long) blockInstanceForm.instance_id());
    if (instance.isEmpty()) {
      return new BlockInstanceResponse(false);
    }
    final InstanceBlock instanceBlock = instanceBlockRepository.findInstanceBlockByInstance(
        instance.get());
    if (blockInstanceForm.block() && instanceBlock == null) {
      instanceBlockRepository.save(InstanceBlock.builder().instance(instance.get()).build());
    } else if (!blockInstanceForm.block()) {
      if (instanceBlock != null) {
        instanceBlockRepository.delete(instanceBlock);
      }
      return new BlockInstanceResponse(false);
    }
    return new BlockInstanceResponse(true);
  }
}
