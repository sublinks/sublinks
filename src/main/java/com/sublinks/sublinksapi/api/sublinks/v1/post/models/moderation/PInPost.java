package com.sublinks.sublinksapi.api.sublinks.v1.post.models.moderation;

public record PInPost(
    String reason,
    Boolean remove) {

  @Override
  public Boolean remove() {

    return remove == null || remove;
  }
}
