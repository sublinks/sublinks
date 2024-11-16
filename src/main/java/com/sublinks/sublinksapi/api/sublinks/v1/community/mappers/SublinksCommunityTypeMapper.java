package com.sublinks.sublinksapi.api.sublinks.v1.community.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.community.enums.SublinksPersonCommunityType;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.moderation.CommunityModeratorResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.mappers.SublinksPersonMapper;
import com.sublinks.sublinksapi.person.entities.LinkPersonCommunity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {SublinksPersonMapper.class,
    SublinksPersonCommunityType.class})
public abstract class SublinksCommunityTypeMapper implements
    Converter<LinkPersonCommunity, CommunityModeratorResponse> {

  @Override
  @Mapping(target = "person", source = "linkPersonCommunity.person")
  @Mapping(target = "linkType", source = "linkPersonCommunity.linkType")
  public abstract CommunityModeratorResponse convert(
      @Nullable LinkPersonCommunity linkPersonCommunity);
}
