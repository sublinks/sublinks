package com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record RemoveAnnouncement(
    @Schema(description = "The new content of the announcement",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "true") Boolean removed,
    @Schema(description = "The reason for removing the announcement",
        requiredMode = RequiredMode.NOT_REQUIRED) String reason) {

  @Override
  public Boolean removed() {

    return removed == null || removed;
  }
}
