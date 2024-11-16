package com.sublinks.sublinksapi.authorization.enums;

public enum RolePermissionPersonTypes implements RolePermissionInterface {

  // Person permissions
  READ_USER("user", AuthorizeAction.READ),
  READ_USERS("users", AuthorizeAction.READ),
  UPDATE_USER("user", AuthorizeAction.UPDATE),
  UPDATE_USER_SETTINGS("user-settings", AuthorizeAction.UPDATE),
  DELETE_USER("user", AuthorizeAction.DELETE),
  PURGE_USER("user", AuthorizeAction.PURGE),
  READ_MENTION_USER("user-mention", AuthorizeAction.READ),
  READ_PERSON_AGGREGATION("user-aggregation", AuthorizeAction.READ),
  MARK_MENTION_AS_READ("user-mention-read", AuthorizeAction.UPDATE),
  READ_REPLIES("user-reply-read", AuthorizeAction.READ),
  MARK_REPLIES_AS_READ("user-reply-read", AuthorizeAction.UPDATE),
  RESET_PASSWORD("user-reset-password", AuthorizeAction.DELETE),
  USER_BLOCK("user", AuthorizeAction.BLOCK),
  USER_LOGIN("user-login", AuthorizeAction.READ),
  USER_EXPORT("user-export", AuthorizeAction.READ),

  READ_USER_METADATA("user-metadata", AuthorizeAction.READ),
  READ_USER_METADATAS("user-metadatas", AuthorizeAction.READ),
  READ_USER_OWN_METADATA("user-own-metadata", AuthorizeAction.READ),
  READ_USER_OWN_METADATAS("user-own-metadatas", AuthorizeAction.READ),
  INVALIDATE_USER_METADATA("user-metadata", AuthorizeAction.UPDATE),
  INVALIDATE_USER_OWN_METADATA("user-own-metadata", AuthorizeAction.UPDATE),
  DELETE_USER_METADATA("user-metadata", AuthorizeAction.DELETE),
  DELETE_USER_OWN_METADATA("user-own-metadata", AuthorizeAction.DELETE),

  /**
   * Unused
   */
  ADMIN_REMOVE_USER("user-admin", AuthorizeAction.REMOVE),
  REPORT_USER("user-report", AuthorizeAction.CREATE);

  public final String Entity;
  public final AuthorizeAction Action;

  RolePermissionPersonTypes(String Entity, AuthorizeAction Action) {

    this.Entity = Entity;
    this.Action = Action;
  }

  @Override
  public String toString() {

    return this.Entity + ":" + this.Action;
  }
}
