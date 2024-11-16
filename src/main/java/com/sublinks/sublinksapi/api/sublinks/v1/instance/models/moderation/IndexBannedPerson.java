package com.sublinks.sublinksapi.api.sublinks.v1.instance.models.moderation;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortOrder;
import lombok.Builder;

@Builder
public record IndexBannedPerson(
    String search,
    Boolean local,
    SortOrder sortOrder,
    Integer limit,
    Integer page) {

}
