package com.sublinks.sublinksapi.api.sublinks.v1.person.models;

import com.sublinks.sublinksapi.api.sublinks.v1.languages.models.LanguageResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record CreatePerson(
    String name,
    String displayName,
    String email,
    List<LanguageResponse> languages,
    String avatarImageUrl,
    String bannerImageUrl,
    String bio,
    String matrixUserId,
    String password,
    String passwordConfirmation,
    String answer,
    String captcha_token,
    String captcha_answer) {

}
