package com.sublinks.sublinksapi.api.sublinks.v1.person.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonAggregateResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.PersonKeyUtils;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.entities.PersonAggregate;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@NoArgsConstructor
public abstract class SublinksPersonAggregationMapper implements
    Converter<PersonAggregate, PersonAggregateResponse> {

  @Autowired
  private PersonKeyUtils personKeyUtils;

  @Override
  @Mapping(target = "personKey", source = "person", qualifiedByName = "personKey")
  @Mapping(target = "postCount", source = "postCount")
  @Mapping(target = "commentCount", source = "commentCount")
  @Mapping(target = "postScore", source = "postScore")
  @Mapping(target = "commentScore", source = "commentScore")
  public abstract PersonAggregateResponse convert(@Nullable PersonAggregate personAggregate);

  @Named("personKey")
  String mapPersonKey(Person person) {

    return personKeyUtils.getPersonKey(person);
  }

}
