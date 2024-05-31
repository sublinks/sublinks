package com.sublinks.sublinksapi.api.sublinks.v1.search.models;

import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.CommentResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonResponse;
import java.util.List;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.PostResponse;
import lombok.Builder;

// @todo: Add Communities, Posts, Comments, and Messages
@Builder
public record SearchResponse(
    List<PersonResponse> persons,
    List<CommunityResponse> communities,
    List<PostResponse> posts,
    List<CommentResponse> comments) {

}
