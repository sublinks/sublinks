package com.sublinks.sublinksapi.api.sublinks.v1.person.models;

import lombok.Builder;
import java.util.List;

@Builder
public record PersonSessionDataResponse(
    String personKey,
    List<PersonSessionData> sessions
    ) {

}
