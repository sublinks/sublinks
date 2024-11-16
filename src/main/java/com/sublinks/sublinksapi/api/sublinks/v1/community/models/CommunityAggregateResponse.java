package com.sublinks.sublinksapi.api.sublinks.v1.community.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record CommunityAggregateResponse(
    @Schema(description = "The key of the community",
        requiredMode = RequiredMode.REQUIRED) String communityKey,
    @Schema(description = "The subscriber count of the community",
        requiredMode = RequiredMode.REQUIRED,
        example = "0",
        defaultValue = "0") Integer subscriberCount,
    @Schema(description = "The post count of the community",
        requiredMode = RequiredMode.REQUIRED,
        example = "0",
        defaultValue = "0") Integer postCount,
    @Schema(description = "The comment count of the community",
        requiredMode = RequiredMode.REQUIRED,
        example = "0",
        defaultValue = "0") Integer commentCount,
    @Schema(description = "The active daily user count of the community",
        requiredMode = RequiredMode.REQUIRED,
        example = "0",
        defaultValue = "0") Integer activeDailyUserCount,
    @Schema(description = "The active weekly user count of the community",
        requiredMode = RequiredMode.REQUIRED,
        example = "0",
        defaultValue = "0") Integer activeWeeklyUserCount,
    @Schema(description = "The active monthly user count of the community",
        requiredMode = RequiredMode.REQUIRED,
        example = "0",
        defaultValue = "0") Integer activeMonthlyUserCount,
    @Schema(description = "The active quarterly user count of the community",
        requiredMode = RequiredMode.REQUIRED,
        example = "0",
        defaultValue = "0") Integer activeHalfYearUserCount) {

}
