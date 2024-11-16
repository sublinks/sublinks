package com.sublinks.sublinksapi.api.sublinks.v1.person.models;

import com.sublinks.sublinksapi.api.sublinks.v1.person.enums.RegistrationState;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Builder
public record LoginResponse(
    @Schema(description = "The token to be used in the Authorization header for future requests",
        requiredMode = RequiredMode.NOT_REQUIRED) String token,
    RegistrationState status,
    @Schema(description = "The error message if the login failed",
        requiredMode = RequiredMode.NOT_REQUIRED) String error) {

}
