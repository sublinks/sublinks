package com.sublinks.sublinksapi.api.sublinks.v1.post.models;

public record AggregatePostResponse(
    String key,
    String commentCount,
    String downvoteCount,
    String upvoteCount,
    String score,
    String hotRank,
    String controversyRank) {

}
