package com.sublinks.sublinksapi.api.lemmy.v3.enums.mappers;

import com.sublinks.sublinksapi.person.enums.ListingType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ListingTypeMapper {

  ListingType map(com.sublinks.sublinksapi.api.lemmy.v3.enums.ListingType listingType);
}
