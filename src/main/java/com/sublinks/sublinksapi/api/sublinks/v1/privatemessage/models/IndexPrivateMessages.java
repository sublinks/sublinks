package com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models;

import com.sublinks.sublinksapi.privatemessages.enums.PrivateMessageSortType;

public record IndexPrivateMessages(
    String search,
    Boolean unreadOnly,
    PrivateMessageSortType sort,
    Boolean localOnly,
    String senderKey,
    Integer page,
    Integer perPage) {

  public Integer page() {

    return page != null ? page : 1;
  }

  public Integer perPage() {

    return perPage != null ? perPage : 20;
  }
}
