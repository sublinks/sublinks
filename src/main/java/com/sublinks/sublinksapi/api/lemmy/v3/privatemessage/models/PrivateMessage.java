package com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models;

import lombok.Builder;

@Builder
@SuppressWarnings("RecordComponentName")
public record PrivateMessage(
    Long id,
    Long creator_id,
    Long recipient_id,
    String content,
    boolean deleted,
    boolean read,
    String published,
    String updated,
    String ap_id,
    boolean local
) {

}