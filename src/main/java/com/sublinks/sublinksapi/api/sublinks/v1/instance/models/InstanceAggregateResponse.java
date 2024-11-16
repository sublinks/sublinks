package com.sublinks.sublinksapi.api.sublinks.v1.instance.models;

public record InstanceAggregateResponse(
    String instanceKey,
    Integer userCount,
    Integer postCount,
    Integer commentCount,
    Integer communityCount,
    Integer activeDailyUserCount,
    Integer activeWeeklyUserCount,
    Integer activeMonthlyUserCount,
    Integer activeHalfYearlyUserCount) {

}
