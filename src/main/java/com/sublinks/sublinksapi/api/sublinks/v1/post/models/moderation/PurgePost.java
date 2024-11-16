package com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Builder
public record PurgePost(
    @Schema(description = "The reason for purging the post",
        example = "This post is spam",
        requiredMode = RequiredMode.NOT_REQUIRED) String reason) {

}
