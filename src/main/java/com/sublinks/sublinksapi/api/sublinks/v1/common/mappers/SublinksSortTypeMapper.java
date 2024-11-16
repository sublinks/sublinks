package com.sublinks.sublinksapi.api.sublinks.v1.common.mappers;

import com.sublinks.sublinksapi.api.lemmy.v3.enums.SortType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class SublinksSortTypeMapper implements
    Converter<com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType, SortType> {

  @Nullable
  @Override
  public SortType convert(
      com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType listingType)
  {

    return SortType.valueOf(listingType.name());
  }
}
