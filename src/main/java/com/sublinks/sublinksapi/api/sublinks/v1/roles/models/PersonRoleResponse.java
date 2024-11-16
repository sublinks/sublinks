package com.sublinks.sublinksapi.api.sublinks.v1.roles.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Builder
public record PersonRoleResponse(
    String key,
    String name,
    String description,
    Boolean isActive,
    Boolean isExpired,
    @Schema(
        requiredMode = RequiredMode.NOT_REQUIRED,
        description = "The date and time the role expires at"
    )
    String expiresAt,
    String createdAt,
    String updatedAt) {

}
