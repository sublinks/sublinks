package com.sublinks.sublinksapi.api.sublinks.v1.person.models;

import lombok.Builder;

@Builder
public record PersonAggregateResponse(
    String personKey,
    Integer postCount,
    Integer commentCount,
    Integer postScore,
    Integer commentScore) {

}
