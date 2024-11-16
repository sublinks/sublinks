package com.sublinks.sublinksapi.api.sublinks.v1.person.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Builder
public record LoginPerson(
    @Schema(description = "The username",
        requiredMode = RequiredMode.NOT_REQUIRED,
        example = "john_doe") String username,
    @Schema(description = "The password",
        requiredMode = RequiredMode.NOT_REQUIRED,
        example = "topsecretpasswordnooneknows") String password,
    @Schema(description = "The captcha token",
        requiredMode = RequiredMode.NOT_REQUIRED,
        example = "03AGdBq26") String captcha_token,
    @Schema(description = "The captcha answer",
        requiredMode = RequiredMode.NOT_REQUIRED,
        example = "3") String captcha_answer) {

  public LoginPerson {

    if (username == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username_required");
    }
    if (password == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password_required");
    }
  }

}
