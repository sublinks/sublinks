package com.sublinks.sublinksapi.api.sublinks.v1.person.models;

import lombok.Builder;

@Builder
public record PersonSessionData(
    Long key,
    String ipAddress,
    String userAgent,
    Boolean active,
    String createdAt,
    String updatedAt) {

}
