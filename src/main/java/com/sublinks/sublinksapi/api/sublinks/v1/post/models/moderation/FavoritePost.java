package com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation;

import io.swagger.v3.oas.annotations.media.Schema;

public record FavoritePost(
    @Schema(description = "If the post is favourited by you",
        defaultValue = "true",
        example = "true") Boolean favorite) {

  @Override
  public Boolean favorite() {

    return favorite == null || favorite;
  }
}
