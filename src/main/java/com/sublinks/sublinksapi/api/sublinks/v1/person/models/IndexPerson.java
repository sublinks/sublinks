package com.sublinks.sublinksapi.api.sublinks.v1.person.models;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SublinksListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Builder
public record IndexPerson(
    @Schema(description = "The search query",
        requiredMode = RequiredMode.NOT_REQUIRED) String search,
    @Schema(description = "The listing type",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "All") SublinksListingType listingType,
    @Schema(description = "The sort type",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "New") SortType sortType,
    @Schema(description = "The page",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "1") Integer page,
    @Schema(description = "The number of items per page",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "20") Integer perPage) {

  @Override
  public SublinksListingType listingType() {
    return listingType == null ? SublinksListingType.Local : listingType;
  }

  @Override
  public SortType sortType() {
    return sortType == null ? SortType.New : sortType;
  }

  @Override
  public Integer page() {
    return page == null ? 0 : page;
  }

  @Override
  public Integer perPage() {
    return perPage == null ? 20 : perPage;
  }
}
