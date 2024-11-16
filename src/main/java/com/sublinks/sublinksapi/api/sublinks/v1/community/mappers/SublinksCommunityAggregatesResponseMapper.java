package com.sublinks.sublinksapi.api.sublinks.v1.community.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CommunityAggregateResponse;
import com.sublinks.sublinksapi.community.entities.CommunityAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class SublinksCommunityAggregatesResponseMapper implements
    Converter<CommunityAggregate, CommunityAggregateResponse> {

  @Override
  @Mapping(target = "communityKey", source = "communityAggregate.community.titleSlug")
  @Mapping(target = "subscriberCount", source = "communityAggregate.subscriberCount")
  @Mapping(target = "postCount", source = "communityAggregate.postCount")
  @Mapping(target = "commentCount", source = "communityAggregate.commentCount")
  @Mapping(target = "activeDailyUserCount", source = "communityAggregate.activeDailyUserCount")
  @Mapping(target = "activeWeeklyUserCount", source = "communityAggregate.activeWeeklyUserCount")
  @Mapping(target = "activeMonthlyUserCount", source = "communityAggregate.activeMonthlyUserCount")
  @Mapping(target = "activeHalfYearUserCount",
      source = "communityAggregate.activeHalfYearUserCount")
  public abstract CommunityAggregateResponse convert(
      @Nullable CommunityAggregate communityAggregate);
}
