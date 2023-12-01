package com.sublinks.sublinksapi.api.lemmy.v3.site.services;

import com.sublinks.sublinksapi.api.lemmy.v3.customemoji.models.CustomEmojiView;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.Language;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.LocalSite;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.LocalSiteRateLimit;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.Site;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.SiteAggregates;
import com.sublinks.sublinksapi.api.lemmy.v3.site.models.SiteView;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.PersonView;
import com.sublinks.sublinksapi.api.lemmy.v3.user.services.LemmyPersonService;
import com.sublinks.sublinksapi.instance.dto.InstanceConfig;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.language.repositories.LanguageRepository;
import com.sublinks.sublinksapi.person.dto.LinkPersonInstance;
import com.sublinks.sublinksapi.person.enums.LinkPersonInstanceType;
import com.sublinks.sublinksapi.person.repositories.LinkPersonInstanceRepository;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LemmySiteService {

  private final ConversionService conversionService;
  private final LocalInstanceContext localInstanceContext;
  private final LemmyPersonService lemmyPersonService;
  private final LinkPersonInstanceRepository linkPersonInstanceRepository;

  // @todo finish admin list
  public Collection<PersonView> admins() {

    Collection<LinkPersonInstance> admins = linkPersonInstanceRepository.getLinkPersonInstancesByInstanceAndLinkTypeIsIn(
        localInstanceContext.instance(),
        List.of(LinkPersonInstanceType.admin, LinkPersonInstanceType.super_admin));
    final Collection<PersonView> adminViews = new LinkedHashSet<>();
    for (LinkPersonInstance linkPersonInstance : admins) {
      adminViews.add(lemmyPersonService.getPersonView(linkPersonInstance.getPerson()));
    }
    return adminViews;
  }

  public Collection<Language> allLanguages(final LanguageRepository languageRepository) {

    final Collection<Language> languages = new LinkedHashSet<>();
    for (com.sublinks.sublinksapi.language.dto.Language language : languageRepository.findAll()) {
      Language l = Language.builder().id(language.getId()).code(language.getCode())
          .name(language.getName()).build();
      languages.add(l);
    }
    return languages;
  }

  // @todo custom emojis
  public Collection<CustomEmojiView> customEmojis() {

    final Collection<CustomEmojiView> emojiViews = new LinkedHashSet<>();
    return emojiViews;
  }

  public SiteView getSiteView() {

    final LocalSite localSite = conversionService.convert(localInstanceContext, LocalSite.class);

    final LocalSite.LocalSiteBuilder builder = localSite.toBuilder();

    if (localInstanceContext.instance().getInstanceConfig() != null) {
      InstanceConfig config = localInstanceContext.instance().getInstanceConfig();
      builder.application_question(config.getRegistrationQuestion());
      builder.registration_mode(config.getRegistrationMode());
      builder.private_instance(config.isPrivateInstance());
      builder.require_email_verification(config.isRequireEmailVerification());
      builder.enable_downvotes(config.isEnableDownvotes());
      builder.enable_nsfw(config.isEnableNsfw());
      builder.community_creation_admin_only(config.isCommunityCreationAdminOnly());
      builder.application_email_admins(config.isApplicationEmailAdmins());
      builder.hide_modlog_mod_names(config.isHideModlogModNames());
      builder.federation_enabled(config.isFederationEnabled());
      builder.captcha_enabled(config.isCaptchaEnabled());
      builder.captcha_difficulty(config.getCaptchaDifficulty());
      builder.legal_information(config.getLegalInformation());
      builder.default_post_listing_type(config.getDefaultPostListingType());
      builder.actor_name_max_length(config.getActorNameMaxLength());
      builder.default_theme(config.getDefaultTheme());
      builder.slur_filter_regex(config.getSlurFilterRegex());
    }

    return SiteView.builder().site(conversionService.convert(localInstanceContext, Site.class))
        .local_site(builder.build())
        .counts(conversionService.convert(localInstanceContext, SiteAggregates.class))
        .local_site_rate_limit(
            conversionService.convert(localInstanceContext, LocalSiteRateLimit.class)).build();
  }
}
