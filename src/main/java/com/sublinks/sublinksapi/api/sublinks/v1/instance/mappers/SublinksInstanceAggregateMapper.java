package com.sublinks.sublinksapi.api.sublinks.v1.instance.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceAggregateResponse;
import com.sublinks.sublinksapi.instance.entities.InstanceAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class SublinksInstanceAggregateMapper implements
    Converter<InstanceAggregate, InstanceAggregateResponse> {

  @Override
  @Mapping(target = "instanceKey", source = "instanceAggregate.instance.domain")
  @Mapping(target = "userCount", source = "instanceAggregate.userCount")
  @Mapping(target = "postCount", source = "instanceAggregate.postCount")
  @Mapping(target = "commentCount", source = "instanceAggregate.commentCount")
  @Mapping(target = "communityCount", source = "instanceAggregate.communityCount")
  @Mapping(target = "activeDailyUserCount", source = "instanceAggregate.activeDailyUserCount")
  @Mapping(target = "activeWeeklyUserCount", source = "instanceAggregate.activeWeeklyUserCount")
  @Mapping(target = "activeMonthlyUserCount", source = "instanceAggregate.activeMonthlyUserCount")
  @Mapping(target = "activeHalfYearlyUserCount",
      source = "instanceAggregate.activeHalfYearUserCount")
  public abstract InstanceAggregateResponse convert(@Nullable InstanceAggregate instanceAggregate);
}
