package com.sublinks.sublinksapi.api.sublinks.v1.comment.models.Moderation;

public record PurgeComment(
    String reason,
    Boolean remove) {

}
