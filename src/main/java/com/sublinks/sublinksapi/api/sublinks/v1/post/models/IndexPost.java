package com.sublinks.sublinksapi.api.sublinks.v1.post.models;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SublinksListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import lombok.Builder;

@Builder
public record IndexPost(
    @Schema(description = "Search query", requiredMode = RequiredMode.NOT_REQUIRED) String search,
    @Schema(description = "Sort type", requiredMode = RequiredMode.NOT_REQUIRED) SortType sortType,
    @Schema(description = "Listing type",
        requiredMode = RequiredMode.NOT_REQUIRED) SublinksListingType listingType,
    @Schema(description = "Community keys",
        requiredMode = RequiredMode.NOT_REQUIRED) List<String> communityKeys,
    @Schema(description = "Show NSFW", requiredMode = RequiredMode.NOT_REQUIRED) Boolean showNsfw,
    @Schema(description = "Saved only", requiredMode = RequiredMode.NOT_REQUIRED) Boolean savedOnly,
    String pageCursor,
    @Schema(description = "Per page", requiredMode = RequiredMode.NOT_REQUIRED) Integer perPage,
    @Schema(description = "Page", requiredMode = RequiredMode.NOT_REQUIRED) Integer page) {

  @Override
  public Integer perPage() {

    return perPage != null ? perPage : 20;
  }

  @Override
  public Integer page() {

    return page != null ? page : 0;
  }
}
