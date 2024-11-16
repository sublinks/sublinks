package com.sublinks.sublinksapi.authorization.enums;

public enum RolePermissionRoleTypes implements RolePermissionInterface {

  // Person permissions
  ROLE_READ("role", AuthorizeAction.READ),
  ROLES_READ("roles", AuthorizeAction.READ),
  ROLE_CREATE("role", AuthorizeAction.CREATE),
  ROLE_UPDATE("role", AuthorizeAction.UPDATE),
  ROLE_DELETE("role", AuthorizeAction.DELETE);


  public final String Entity;
  public final AuthorizeAction Action;

  RolePermissionRoleTypes(String Entity, AuthorizeAction Action) {

    this.Entity = Entity;
    this.Action = Action;
  }

  @Override
  public String toString() {

    return this.Entity + ":" + this.Action;
  }
}
