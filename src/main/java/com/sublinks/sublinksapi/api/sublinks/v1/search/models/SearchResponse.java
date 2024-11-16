package com.sublinks.sublinksapi.api.sublinks.v1.search.models;

import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.CommentResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.PostResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models.PrivateMessageResponse;
import java.util.List;
import lombok.Builder;

// @todo: Add Communities, Posts, Comments, and Messages
@Builder
public record SearchResponse(
    List<PersonResponse> persons,
    List<CommunityResponse> communities,
    List<PostResponse> posts,
    List<CommentResponse> comments,
    List<InstanceResponse> instances,
    List<PrivateMessageResponse> messages) {

}
