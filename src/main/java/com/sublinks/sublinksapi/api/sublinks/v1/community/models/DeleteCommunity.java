package com.sublinks.sublinksapi.api.sublinks.v1.community.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record DeleteCommunity(
    @Schema(description = "The reason for deleting the community",
        requiredMode = RequiredMode.NOT_REQUIRED) String reason,
    @Schema(description = "The boolean to remove the community",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "true") Boolean remove) {

}
