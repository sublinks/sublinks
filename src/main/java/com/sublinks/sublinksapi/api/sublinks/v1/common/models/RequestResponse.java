package com.sublinks.sublinksapi.api.sublinks.v1.common.models;

import lombok.Builder;

@Builder
public record RequestResponse(
    Boolean success,
    String error) {

}
