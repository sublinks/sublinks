package com.sublinks.sublinksapi.api.sublinks.v1.instance.models;

import lombok.Builder;

@Builder
public record IndexInstance(
    String search,
    Integer page,
    Integer perPage) {

}
