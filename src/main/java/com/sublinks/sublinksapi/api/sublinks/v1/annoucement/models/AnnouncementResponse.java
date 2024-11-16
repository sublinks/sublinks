package com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record AnnouncementResponse(
    @Schema(description = "The key of the announcement",
        requiredMode = RequiredMode.REQUIRED,
        example = "1") String key,
    @Schema(description = "The content of the announcement",
        requiredMode = RequiredMode.REQUIRED,
        example = "This is an announcement") String content,
    @Schema(description = "The created at date",
        requiredMode = RequiredMode.REQUIRED,
        example = "2021-01-01T00:00:00Z") String createdAt,
    @Schema(
        description = "The active status of the announcement",
        requiredMode = RequiredMode.REQUIRED,
        example = "true"
    ) Boolean active,
    @Schema(description = "The updated at date",
        requiredMode = RequiredMode.REQUIRED,
        example = "2021-01-01T00:00:00Z") String updatedAt) {

}
