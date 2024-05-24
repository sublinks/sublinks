package com.sublinks.sublinksapi.api.lemmy.v3.privateMessage.models;

import lombok.Builder;

@Builder
public record PrivateMessageReport(
    Long id,
    Long creator_id,
    Long private_message_id,
    String original_pm_text,
    String reason,
    boolean resolved,
    Long resolver_id,
    String published,
    String updated
) {

}