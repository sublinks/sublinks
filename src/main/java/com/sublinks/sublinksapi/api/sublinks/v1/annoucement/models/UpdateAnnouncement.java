package com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record UpdateAnnouncement(
    @Schema(description = "The new content of the announcement",
        requiredMode = RequiredMode.NOT_REQUIRED) String content,
    @Schema(description = "The new active status of the announcement",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "true") Boolean active) {

}
