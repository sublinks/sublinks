package com.sublinks.sublinksapi.api.sublinks.v1.roles.models;

import com.sublinks.sublinksapi.authorization.enums.RolePermissionInterface;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Set;
import lombok.Builder;

@Builder
public record RoleResponse(
    String key,
    String name,
    String description,
    Set<RolePermissionInterface> permissions,
    @Schema(requiredMode = RequiredMode.REQUIRED,
        description = "The permissions this role inherits") Set<RolePermissionInterface> inheritedPermissions,
    @Schema(requiredMode = RequiredMode.NOT_REQUIRED,
        description = "The role this role inherits from") String inheritsFrom,
    Boolean isActive,
    Boolean isExpired,
    @Schema(requiredMode = RequiredMode.NOT_REQUIRED,
        description = "The date and time the role expires at") String expiresAt,
    String createdAt,
    String updatedAt) {

}
