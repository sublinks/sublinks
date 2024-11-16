package com.sublinks.sublinksapi.api.sublinks.v1.common.mappers;

import com.sublinks.sublinksapi.api.lemmy.v3.enums.ListingType;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SublinksListingType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class LemmyListingTypeMapper implements
    Converter<SublinksListingType, ListingType> {

  @Nullable
  @Override
  public ListingType convert(
      SublinksListingType sublinksListingType)
  {

    return ListingType.valueOf(sublinksListingType.name());
  }


}
