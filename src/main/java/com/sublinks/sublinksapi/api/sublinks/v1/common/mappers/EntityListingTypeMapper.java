package com.sublinks.sublinksapi.api.sublinks.v1.common.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SublinksListingType;
import com.sublinks.sublinksapi.person.enums.ListingType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class EntityListingTypeMapper implements
    Converter<ListingType, SublinksListingType> {

  @Nullable
  @Override
  public SublinksListingType convert(
      ListingType listingType)
  {

    return SublinksListingType.valueOf(
        listingType.name());
  }


}
