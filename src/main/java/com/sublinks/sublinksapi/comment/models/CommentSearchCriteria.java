package com.sublinks.sublinksapi.comment.models;

import com.sublinks.sublinksapi.comment.entities.Comment;
import com.sublinks.sublinksapi.comment.enums.CommentSortType;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.ListingType;
import com.sublinks.sublinksapi.post.entities.Post;
import lombok.Builder;

@Builder
public record CommentSearchCriteria(
    String search,
    ListingType listingType,
    CommentSortType commentSortType,
    Integer perPage,
    Integer page,
    Integer maxDepth,
    Community community,
    Post post,
    Comment parent,
    Boolean savedOnly,
    Person person) {

}
