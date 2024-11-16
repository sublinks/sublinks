package com.sublinks.sublinksapi.api.sublinks.v1.comment.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Builder
public record DeleteComment(
    @Schema(requiredMode = RequiredMode.REQUIRED,
        description = "The reason for deleting the comment",
        example = "Spam") String reason,
    @Schema(requiredMode = RequiredMode.NOT_REQUIRED,
        description = "Whether to remove the comment",
        example = "true",
        defaultValue = "true") Boolean remove) {

  @Override
  public Boolean remove() {

    return remove == null || remove;
  }
}
