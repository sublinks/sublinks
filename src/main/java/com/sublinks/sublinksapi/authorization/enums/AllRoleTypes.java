package com.sublinks.sublinksapi.authorization.enums;

import java.util.ArrayList;
import java.util.List;

public class AllRoleTypes {

  public static final List<RolePermissionInterface> ALL_ROLE_TYPES = getAuthorizedEntityTypes();

  public static List<RolePermissionInterface> getAuthorizedEntityTypes() {

    final List<RolePermissionInterface> permissions = new ArrayList<>();

    permissions.addAll(List.of(RolePermissionRoleTypes.values()));
    permissions.addAll(List.of(RolePermissionPostTypes.values()));
    permissions.addAll(List.of(RolePermissionCommentTypes.values()));
    permissions.addAll(List.of(RolePermissionPrivateMessageTypes.values()));
    permissions.addAll(List.of(RolePermissionInstanceTypes.values()));
    permissions.addAll(List.of(RolePermissionCommunityTypes.values()));
    permissions.addAll(List.of(RolePermissionEmojiTypes.values()));
    permissions.addAll(List.of(RolePermissionMediaTypes.values()));
    permissions.addAll(List.of(RolePermissionModLogTypes.values()));
    permissions.addAll(List.of(RolePermissionPersonTypes.values()));

    return permissions;
  }
}
