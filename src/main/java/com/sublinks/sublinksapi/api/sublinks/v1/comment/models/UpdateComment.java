package com.sublinks.sublinksapi.api.sublinks.v1.comment.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Builder
public record UpdateComment(
    @Schema(description = "The new content of the comment",
        requiredMode = RequiredMode.REQUIRED) String body,
    @Schema(description = "The language key of the comment",
        defaultValue = "und",
        example = "und",
        requiredMode = RequiredMode.NOT_REQUIRED) String languageKey,
    @Schema(description = "Whether the comment is featured ( Requires Moderator or Admin )",
        example = "false",
        requiredMode = RequiredMode.NOT_REQUIRED) Boolean featured) {

}
