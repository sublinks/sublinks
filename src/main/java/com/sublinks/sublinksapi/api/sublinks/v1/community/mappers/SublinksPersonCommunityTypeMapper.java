package com.sublinks.sublinksapi.api.sublinks.v1.community.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.community.enums.SublinksPersonCommunityType;
import com.sublinks.sublinksapi.api.sublinks.v1.languages.mappers.SublinksLanguageMapper;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.mappers.OptionalStringMapper;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
    SublinksLanguageMapper.class, OptionalStringMapper.class})
public abstract class SublinksPersonCommunityTypeMapper implements
    Converter<LinkPersonCommunityType, SublinksPersonCommunityType> {

  @Override
  public SublinksPersonCommunityType convert(
      @Nullable LinkPersonCommunityType linkPersonCommunityType)
  {

    if (linkPersonCommunityType == null) {
      return null;
    }
    return SublinksPersonCommunityType.valueOf(linkPersonCommunityType.name());

  }
}
