package com.sublinks.sublinksapi.api.sublinks.v1.comment.models;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SublinksListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Builder
public record IndexComment(
    @Schema(description = "Search query", requiredMode = RequiredMode.NOT_REQUIRED) String search,
    @Schema(description = "Sort type", requiredMode = RequiredMode.NOT_REQUIRED) SortType sortType,
    @Schema(description = "Listing type",
        requiredMode = RequiredMode.NOT_REQUIRED) SublinksListingType listingType,
    @Schema(description = "Community key",
        requiredMode = RequiredMode.NOT_REQUIRED) String communityKey,
    @Schema(description = "Post key", requiredMode = RequiredMode.NOT_REQUIRED) String postKey,
    @Schema(description = "Parent Comment key",
        requiredMode = RequiredMode.NOT_REQUIRED) String parentCommentKey,
    @Schema(description = "Show NSFW", requiredMode = RequiredMode.NOT_REQUIRED) Boolean showNsfw,
    @Schema(description = "Saved only", requiredMode = RequiredMode.NOT_REQUIRED) Boolean savedOnly,
    @Schema(description = "Max Depth",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "3") Integer maxDepth,
    @Schema(description = "Per page", requiredMode = RequiredMode.NOT_REQUIRED) Integer perPage,
    @Schema(description = "Page", requiredMode = RequiredMode.NOT_REQUIRED) Integer page) {

  @Override
  public Integer perPage() {

    return perPage != null ? perPage : 20;
  }

  @Override
  public Integer page() {

    return page != null ? page : 1;
  }
}
