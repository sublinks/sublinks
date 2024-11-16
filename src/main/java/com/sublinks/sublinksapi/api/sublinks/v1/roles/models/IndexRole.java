package com.sublinks.sublinksapi.api.sublinks.v1.roles.models;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortOrder;
import lombok.Builder;

@Builder
public record IndexRole(
    String search,
    Integer page,
    Integer limit,
    SortOrder sort) {

}
