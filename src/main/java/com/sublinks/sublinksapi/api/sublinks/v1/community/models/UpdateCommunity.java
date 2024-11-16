package com.sublinks.sublinksapi.api.sublinks.v1.community.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record UpdateCommunity(
    @Schema(description = "The title of the community",
        requiredMode = RequiredMode.NOT_REQUIRED) String title,
    @Schema(description = "The description of the community",
        requiredMode = RequiredMode.NOT_REQUIRED) String description,
    @Schema(description = "The icon image URL of the community",
        requiredMode = RequiredMode.NOT_REQUIRED) String iconImageUrl,
    @Schema(description = "The banner image URL of the community",
        requiredMode = RequiredMode.NOT_REQUIRED) String bannerImageUrl,
    @Schema(description = "The active status of the community",
        requiredMode = RequiredMode.NOT_REQUIRED) Boolean isNsfw,
    @Schema(description = "Is the community restricted to moderators",
        requiredMode = RequiredMode.NOT_REQUIRED) Boolean isPostingRestrictedToMods) {

}
