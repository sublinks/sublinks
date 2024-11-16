package com.sublinks.sublinksapi.api.sublinks.v1.community.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.languages.mappers.SublinksLanguageMapper;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.DateUtils;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.mappers.OptionalStringMapper;
import com.sublinks.sublinksapi.community.entities.Community;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
    SublinksLanguageMapper.class, OptionalStringMapper.class})
public abstract class SublinksCommunityMapper implements Converter<Community, CommunityResponse> {

  @Override
  @Mapping(target = "key", source = "community.titleSlug")
  @Mapping(target = "title", source = "community.title")
  @Mapping(target = "titleSlug", source = "community.titleSlug")
  @Mapping(target = "description", source = "community.description")
  @Mapping(target = "iconImageUrl", source = "community.iconImageUrl")
  @Mapping(target = "bannerImageUrl", source = "community.bannerImageUrl")
  @Mapping(target = "activityPubId", source = "community.activityPubId")
  @Mapping(target = "languages", source = "community.languages")
  @Mapping(target = "isLocal", source = "community.local")
  @Mapping(target = "isDeleted", source = "community.deleted")
  @Mapping(target = "isRemoved", source = "community.removed")
  @Mapping(target = "isNsfw", source = "community.nsfw")
  @Mapping(target = "restrictedToModerators", source = "community.postingRestrictedToMods")
  @Mapping(target = "publicKey", source = "community.publicKey")
  @Mapping(target = "createdAt",
      source = "community.createdAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "updatedAt",
      source = "community.updatedAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  public abstract CommunityResponse convert(@Nullable Community community);
}
