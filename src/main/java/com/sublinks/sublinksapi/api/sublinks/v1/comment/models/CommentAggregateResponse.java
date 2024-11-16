package com.sublinks.sublinksapi.api.sublinks.v1.comment.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Builder
public record CommentAggregateResponse(
    @Schema(description = "Search query",
        requiredMode = RequiredMode.NOT_REQUIRED) String commentKey,
    @Schema(description = "The number of upvotes",
        requiredMode = RequiredMode.NOT_REQUIRED) Integer upVotes,
    @Schema(description = "The number of downvotes",
        requiredMode = RequiredMode.NOT_REQUIRED) Integer downVotes,
    @Schema(description = "The number of hot rank",
        requiredMode = RequiredMode.NOT_REQUIRED) Integer hotRank,
    @Schema(description = "The number of controversy rank",
        requiredMode = RequiredMode.NOT_REQUIRED) Integer controversyRank,
    @Schema(description = "The number of reply count",
        requiredMode = RequiredMode.NOT_REQUIRED) Integer replyCount) {

}
