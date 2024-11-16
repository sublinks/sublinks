package com.sublinks.sublinksapi.api.sublinks.v1.community.models;

import com.sublinks.sublinksapi.api.sublinks.v1.languages.models.LanguageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import lombok.Builder;

@Builder
public record CommunityResponse(
    @Schema(description = "The key of the community",
        requiredMode = RequiredMode.REQUIRED) String key,
    @Schema(description = "The name of the community",
        requiredMode = RequiredMode.REQUIRED) String title,
    @Schema(description = "The slug of the community",
        requiredMode = RequiredMode.REQUIRED) String titleSlug,
    String description,
    @Schema(description = "The icon image URL of the community",
        requiredMode = RequiredMode.NOT_REQUIRED) String iconImageUrl,
    String bannerImageUrl,
    @Schema(description = "The activityPub ID of the community",
        requiredMode = RequiredMode.REQUIRED) String activityPubId,
    @Schema(description = "The activityPub followers count of the community",
        requiredMode = RequiredMode.NOT_REQUIRED) List<LanguageResponse> languages,
    Boolean isLocal,
    @Schema(description = "Is the community deleted",
        requiredMode = RequiredMode.REQUIRED,
        defaultValue = "false") Boolean isDeleted,
    @Schema(description = "Is the community featured",
        requiredMode = RequiredMode.REQUIRED,
        defaultValue = "false") Boolean isRemoved,
    @Schema(description = "Is the community nsfw",
        requiredMode = RequiredMode.REQUIRED,
        defaultValue = "false") Boolean isNsfw,
    @Schema(description = "Is the community restricted to moderators",
        requiredMode = RequiredMode.REQUIRED,
        defaultValue = "false") Boolean restrictedToModerators,
    @Schema(description = "Public key of the community",
        requiredMode = RequiredMode.REQUIRED) String publicKey,
    @Schema(description = "The creation date of the community",
        requiredMode = RequiredMode.REQUIRED) String createdAt,
    @Schema(description = "The update date of the community",
        requiredMode = RequiredMode.REQUIRED) String updatedAt

) {

}
