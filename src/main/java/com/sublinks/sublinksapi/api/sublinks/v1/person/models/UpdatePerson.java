package com.sublinks.sublinksapi.api.sublinks.v1.person.models;

import java.util.List;
import lombok.Builder;

@Builder
public record UpdatePerson(
    String displayName,
    String email,
    List<String> languagesKeys,
    String avatarImageUrl,
    String bannerImageUrl,
    String bio,
    String matrixUserId,
    String oldPassword,
    String password,
    String passwordConfirmation) {

}
