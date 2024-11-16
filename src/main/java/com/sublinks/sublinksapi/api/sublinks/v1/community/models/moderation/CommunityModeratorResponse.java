package com.sublinks.sublinksapi.api.sublinks.v1.community.models.moderation;

import com.sublinks.sublinksapi.api.sublinks.v1.community.enums.SublinksPersonCommunityType;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonResponse;
import lombok.Builder;

@Builder
public record CommunityModeratorResponse(
    PersonResponse person,
    SublinksPersonCommunityType linkType) {

}
