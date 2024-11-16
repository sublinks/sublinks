package com.sublinks.sublinksapi.community.models;

import com.sublinks.sublinksapi.api.lemmy.v3.enums.ListingType;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.SortType;
import com.sublinks.sublinksapi.person.entities.Person;
import lombok.Builder;

@Builder
public record CommunitySearchCriteria(
    String search,
    SortType sortType,
    ListingType listingType,
    int perPage,
    int page,
    Boolean showNsfw,
    Person person) {

}
