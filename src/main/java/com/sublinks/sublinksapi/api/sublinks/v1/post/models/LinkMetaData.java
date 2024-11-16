package com.sublinks.sublinksapi.api.sublinks.v1.post.models;

import lombok.Builder;

@Builder
public record LinkMetaData(
    String postKey,
    String linkUrl,
    String linkTitle,
    String linkDescription,
    String linkThumbnailUrl,
    String LinkVideoUrl) {

}
