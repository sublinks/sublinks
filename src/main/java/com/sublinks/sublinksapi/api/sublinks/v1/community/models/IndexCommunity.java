package com.sublinks.sublinksapi.api.sublinks.v1.community.models;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SublinksListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Builder
public record IndexCommunity(
    @Schema(description = "Search query",
        example = "Search query",
        requiredMode = RequiredMode.NOT_REQUIRED) String search,
    @Schema(description = "Sort type",
        example = "Hot",
        requiredMode = RequiredMode.NOT_REQUIRED) SortType sortType,
    @Schema(description = "Sublinks listing type",
        example = "All",
        requiredMode = RequiredMode.NOT_REQUIRED) SublinksListingType listingType,
    @Schema(description = "Show NSFW",
        example = "false",
        defaultValue = "false",
        requiredMode = RequiredMode.NOT_REQUIRED) Boolean showNsfw,
    Integer perPage,
    @Schema(description = "Page",
        example = "1",
        defaultValue = "1",
        requiredMode = RequiredMode.NOT_REQUIRED) Integer page) {

  @Override
  public Integer perPage() {

    return perPage != null ? perPage : 20;
  }

  @Override
  public Integer page() {

    return page != null ? page : 1;
  }
}
