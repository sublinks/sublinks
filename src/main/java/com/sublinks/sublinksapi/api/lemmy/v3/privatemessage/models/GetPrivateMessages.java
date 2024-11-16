package com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models;

import java.util.Optional;
import lombok.Builder;

@Builder
@SuppressWarnings("RecordComponentName")
public record GetPrivateMessages(
    Integer page,
    Integer limit,
    Long creator_id,
    Boolean unread_only
) {

}