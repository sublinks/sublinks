package com.sublinks.sublinksapi.api.sublinks.v1.instance.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceConfigResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.DateUtils;
import com.sublinks.sublinksapi.instance.entities.InstanceConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class SublinksInstanceConfigMapper implements
    Converter<InstanceConfig, InstanceConfigResponse> {

  @Override
  @Mapping(target = "instanceKey", source = "instanceConfig.instance.domain")
  @Mapping(target = "registrationMode", source = "instanceConfig.registrationMode")
  @Mapping(target = "registrationQuestion", source = "instanceConfig.registrationQuestion")
  @Mapping(target = "privateInstance", source = "instanceConfig.privateInstance")
  @Mapping(target = "requireEmailVerification", source = "instanceConfig.requireEmailVerification")
  @Mapping(target = "enableDownvotes", source = "instanceConfig.enableDownvotes")
  @Mapping(target = "enableNsfw", source = "instanceConfig.enableNsfw")
  @Mapping(target = "communityCreationAdminOnly",
      source = "instanceConfig.communityCreationAdminOnly")
  @Mapping(target = "applicationEmailAdmins", source = "instanceConfig.applicationEmailAdmins")
  @Mapping(target = "reportEmailAdmins", source = "instanceConfig.reportEmailAdmins")
  @Mapping(target = "hideModlogModNames", source = "instanceConfig.hideModlogModNames")
  @Mapping(target = "federationEnabled", source = "instanceConfig.federationEnabled")
  @Mapping(target = "captchaEnabled", source = "instanceConfig.captchaEnabled")
  @Mapping(target = "captchaDifficulty", source = "instanceConfig.captchaDifficulty")
  @Mapping(target = "nameMaxLength", source = "instanceConfig.actorNameMaxLength")
  @Mapping(target = "defaultTheme", source = "instanceConfig.defaultTheme")
  @Mapping(target = "defaultPostListingType", source = "instanceConfig.defaultPostListingType")
  @Mapping(target = "legalInformation", source = "instanceConfig.legalInformation")
  @Mapping(target = "createdAt",
      source = "instanceConfig.createdAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "updatedAt",
      source = "instanceConfig.updatedAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  public abstract InstanceConfigResponse convert(@Nullable InstanceConfig instanceConfig);
}
