package com.sublinks.sublinksapi.api.sublinks.v1.search.models;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SublinksListingType;
import com.sublinks.sublinksapi.api.sublinks.v1.search.enums.SearchScope;
import lombok.Builder;
import java.util.Set;

@Builder
public record Search(
    String search,
    SortType type,
    SublinksListingType listingType,
    Set<SearchScope> scopes,
    Boolean showNsfw,
    Boolean savedOnly,
    Integer perPage,
    Integer page) {

}
