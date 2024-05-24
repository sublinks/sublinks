package com.sublinks.sublinksapi.api.lemmy.v3.comment.models;

import lombok.Builder;

@Builder
@SuppressWarnings("RecordComponentName")
public record DeleteComment(
    Integer comment_id,
    Boolean deleted
) {

}