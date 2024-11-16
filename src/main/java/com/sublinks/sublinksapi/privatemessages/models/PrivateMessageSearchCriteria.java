package com.sublinks.sublinksapi.privatemessages.models;

import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.privatemessages.enums.PrivateMessageSortType;
import lombok.Builder;

@Builder
public record PrivateMessageSearchCriteria(
    String search,
    PrivateMessageSortType privateMessageSortType,
    int perPage,
    int page,
    boolean unreadOnly,
    Person person) {

}
