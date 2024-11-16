package com.sublinks.sublinksapi.authorization.enums;

public enum RolePermissionPostTypes implements RolePermissionInterface {

  // Person permissions
  READ_POST("post", AuthorizeAction.READ),
  READ_POST_AGGREGATE("post-aggregate", AuthorizeAction.READ),
  READ_POSTS("posts", AuthorizeAction.READ),
  MARK_POST_AS_READ("post-read", AuthorizeAction.UPDATE),
  CREATE_POST("post", AuthorizeAction.CREATE),
  UPDATE_POST("post", AuthorizeAction.UPDATE),
  DELETE_POST("post", AuthorizeAction.DELETE),
  REMOVE_POST("post", AuthorizeAction.REMOVE),
  PURGE_POST("post", AuthorizeAction.PURGE),
  FAVORITE_POST("post", AuthorizeAction.FAVORITE),

  // Vote permissions
  POST_LIST_VOTES("post-upvote", AuthorizeAction.READ),
  POST_UPVOTE("post-upvote", AuthorizeAction.CREATE),
  POST_DOWNVOTE("post-downvote", AuthorizeAction.CREATE),
  POST_NEUTRALVOTE("post-neutralvote", AuthorizeAction.CREATE),

  // Moderator permissions
  MODERATOR_REMOVE_POST("post-moderator", AuthorizeAction.DELETE),
  MODERATOR_PIN_POST("post-moderator", AuthorizeAction.UPDATE),
  MODERATOR_PURGE_POST("post-moderator", AuthorizeAction.PURGE),
  MODERATOR_SHOW_DELETED_POST("post-moderator", AuthorizeAction.READ),

  // Admin permissions
  ADMIN_SHOW_DELETED_POST("post-admin", AuthorizeAction.READ),
  ADMIN_PIN_POST("post-admin-pin", AuthorizeAction.UPDATE),

  // Report permissions
  REPORT_POST("post-report", AuthorizeAction.CREATE);

  public final String Entity;
  public final AuthorizeAction Action;

  RolePermissionPostTypes(String Entity, AuthorizeAction Action) {

    this.Entity = Entity;
    this.Action = Action;
  }

  @Override
  public String toString() {

    return this.Entity + ":" + this.Action;
  }
}
