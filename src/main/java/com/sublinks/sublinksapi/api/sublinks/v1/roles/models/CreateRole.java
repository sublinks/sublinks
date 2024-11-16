package com.sublinks.sublinksapi.api.sublinks.v1.roles.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record CreateRole(
    @Schema(description = "The name of the role", example = "admin") String name,
    @Schema(description = "The description of the role",
        example = "Administrator") String description,
    @Schema(description = "The active status of the role", example = "true") Boolean active,
    @Schema(description = "The permissions of the role") List<String> permissions,
    @Schema(description = "The expiration date of the role",
        requiredMode = RequiredMode.NOT_REQUIRED) Long expiresAt) {

  public CreateRole {

    if (expiresAt != null && expiresAt < System.currentTimeMillis() / 1000) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "expiration_timestamp_must_be_in_the_future");
    }
  }
}
