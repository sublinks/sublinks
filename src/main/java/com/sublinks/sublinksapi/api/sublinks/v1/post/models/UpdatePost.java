package com.sublinks.sublinksapi.api.sublinks.v1.post.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Builder
public record UpdatePost(
    @Schema(description = "The title of the post",
        requiredMode = RequiredMode.REQUIRED) String title,
    @Schema(description = "The body of the post",
        requiredMode = RequiredMode.NOT_REQUIRED) String body,
    @Schema(description = "The language key of the post",
        defaultValue = "und",
        example = "und",
        requiredMode = RequiredMode.NOT_REQUIRED) String languageKey,
    @Schema(description = "Whether the post is featured ( Requires permission to do so. )",
        defaultValue = "false",
        example = "false",
        requiredMode = RequiredMode.NOT_REQUIRED) Boolean featuredLocal,
    @Schema(description = "Whether the post is featured in the community ( Requires permission to do so. )",
        defaultValue = "false",
        example = "false",
        requiredMode = RequiredMode.NOT_REQUIRED) Boolean featuredCommunity,
    @Schema(description = "Is the post nsfw",
        defaultValue = "false",
        example = "false",
        requiredMode = RequiredMode.NOT_REQUIRED) Boolean nsfw,
    @Schema(description = "The link of the post",
        requiredMode = RequiredMode.NOT_REQUIRED) String link) {

  public UpdatePost {

    if (title.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title_can_not_be_blank");
    }
  }

  public String languageKey() {

    return languageKey == null ? "und" : languageKey;
  }
}
