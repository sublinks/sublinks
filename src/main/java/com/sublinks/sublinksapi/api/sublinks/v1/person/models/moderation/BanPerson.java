package com.sublinks.sublinksapi.api.sublinks.v1.person.models.moderation;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Builder
public record BanPerson(
    @Schema(description = "The person key",
        requiredMode = RequiredMode.REQUIRED) @NotNull() String key,
    @Schema(description = "The reason for the ban",
        requiredMode = RequiredMode.NOT_REQUIRED) String reason,
    @Schema(description = "Ban the user",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "true") Boolean ban,
    @Schema(description = "Remove all data associated with the user",
        requiredMode = RequiredMode.NOT_REQUIRED,
        defaultValue = "false") Boolean removeData,
    @Schema(description = "Unix timestamp when the ban should expire",
        requiredMode = RequiredMode.NOT_REQUIRED) Long expirationTimestamp) {

  public BanPerson {

    if (expirationTimestamp != null && expirationTimestamp < System.currentTimeMillis() / 1000) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "expiration_timestamp_must_be_in_the_future");
    }
  }

  @Override
  public Boolean ban() {

    return ban == null || ban;
  }

  @Override
  public Boolean removeData() {

    return removeData != null && removeData;
  }

}
