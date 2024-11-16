package com.sublinks.sublinksapi.api.sublinks.v1.community.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record CreateCommunity(
    @Schema(description = "The title of the community") String title,
    @Schema(description = "The title slug of the community") String titleSlug,
    @Schema(description = "The description of the community") String description,
    @Schema(description = "The icon image URL of the community",
        requiredMode = RequiredMode.NOT_REQUIRED) String iconImageUrl,
    @Schema(description = "The banner image URL of the community",
        requiredMode = RequiredMode.NOT_REQUIRED) String bannerImageUrl,
    @Schema(description = "The active status of the community") Boolean isNsfw,
    @Schema(description = "The active status of the community") Boolean isPostingRestrictedToMods) {

}
