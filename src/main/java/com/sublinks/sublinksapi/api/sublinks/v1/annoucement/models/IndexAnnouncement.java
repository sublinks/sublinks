package com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortOrder;
import com.sublinks.sublinksapi.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record IndexAnnouncement(
    @Schema(description = "The sort order",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "Asc") SortOrder sortOrder,
    @Schema(description = "The page",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "1") Integer page,
    @Schema(description = "The number of items per page",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "20") Integer perPage) {


  @Override
  public SortOrder sortOrder() {

    return sortOrder == null ? SortOrder.Asc : sortOrder;
  }

  @Override
  public Integer page() {

    return page == null ? 1 : Math.max(1, page);
  }

  @Override
  public Integer perPage() {

    return perPage == null ? 20 : PaginationUtils.Clamp(perPage, 1, 20);
  }
}
