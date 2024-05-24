package com.sublinks.sublinksapi.api.lemmy.v3.user.models;

import lombok.Builder;

@Builder
public record PersonMention(
    Long id,
    Long recipient_id,
    Long comment_id,
    boolean read,
    String published
) {

}