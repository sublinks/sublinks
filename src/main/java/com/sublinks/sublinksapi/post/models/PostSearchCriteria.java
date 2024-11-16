package com.sublinks.sublinksapi.post.models;

import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.ListingType;
import com.sublinks.sublinksapi.person.enums.SortType;
import java.util.List;
import lombok.Builder;

@Builder
public record PostSearchCriteria(
    String search,
    SortType sortType,
    ListingType listingType,
    int perPage,
    int page,
    String cursorBasedPageable,
    List<Long> communityIds,
    Person person,
    Boolean isShowNsfw,
    Boolean isSavedOnly,
    Boolean isLikedOnly,
    Boolean isDislikedOnly) {

}
