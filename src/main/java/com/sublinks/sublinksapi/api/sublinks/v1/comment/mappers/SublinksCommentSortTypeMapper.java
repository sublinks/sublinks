package com.sublinks.sublinksapi.api.sublinks.v1.comment.mappers;

import com.sublinks.sublinksapi.comment.enums.CommentSortType;
import com.sublinks.sublinksapi.person.enums.SortType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import org.mapstruct.extensions.spring.AdapterMethodName;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@AdapterMethodName("SublinksCommentSortTypeMapper")
public abstract class SublinksCommentSortTypeMapper implements
    Converter<SortType, CommentSortType> {

  @Override
  @ValueMapping(source = "Hot", target = "Hot")
  @ValueMapping(source = "New", target = "New")
  @ValueMapping(source = "Old", target = "Old")
  @ValueMapping(source = "Active", target = "Hot")
  @ValueMapping(source = "TopDay", target = "Top")
  @ValueMapping(source = "TopWeek", target = "Top")
  @ValueMapping(source = "TopMonth", target = "Top")
  @ValueMapping(source = "TopYear", target = "Top")
  @ValueMapping(source = "TopAll", target = "Top")
  @ValueMapping(source = "MostComments", target = "Top")
  @ValueMapping(source = "NewComments", target = "Top")
  @ValueMapping(source = "TopHour", target = "Top")
  @ValueMapping(source = "TopSixHour", target = "Top")
  @ValueMapping(source = "TopTwelveHour", target = "Top")
  @ValueMapping(source = "TopThreeMonths", target = "Top")
  @ValueMapping(source = "TopSixMonths", target = "Top")
  @ValueMapping(source = "TopNineMonths", target = "Top")
  @ValueMapping(source = "Controversial", target = "New")
  @ValueMapping(source = "Scaled", target = "New")
  public abstract CommentSortType convert(@Nullable SortType sortType);
}