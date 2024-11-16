package com.sublinks.sublinksapi.authorization.enums;

public enum RolePermissionCommentTypes implements RolePermissionInterface {

  // Person permissions
  READ_COMMENT("comment", AuthorizeAction.READ),
  READ_COMMENTS("comments", AuthorizeAction.READ),
  READ_COMMENT_AGGREGATE("comment-aggregate", AuthorizeAction.READ),
  MARK_COMMENT_AS_READ("comment-read", AuthorizeAction.UPDATE),
  CREATE_COMMENT("comment", AuthorizeAction.CREATE),
  UPDATE_COMMENT("comment", AuthorizeAction.UPDATE),
  DELETE_COMMENT("comment", AuthorizeAction.DELETE),
  REMOVE_COMMENT("comment", AuthorizeAction.REMOVE),
  PURGE_COMMENT("comment", AuthorizeAction.PURGE),
  FAVORITE_COMMENT("comment", AuthorizeAction.FAVORITE),
  COMMENT_LIST_VOTES("comment-upvote", AuthorizeAction.READ),
  COMMENT_UPVOTE("comment-upvote", AuthorizeAction.CREATE),
  COMMENT_DOWNVOTE("comment-downvote", AuthorizeAction.CREATE),
  COMMENT_NEUTRALVOTE("comment-neutralvote", AuthorizeAction.CREATE),
  REPORT_COMMENT("comment-report", AuthorizeAction.CREATE),

  // Moderator permissions
  MODERATOR_REMOVE_COMMENT("comment-moderator", AuthorizeAction.DELETE),
  MODERATOR_PIN_COMMENT("comment-moderator", AuthorizeAction.UPDATE),
  MODERATOR_PURGE_COMMENT("comment-moderator", AuthorizeAction.PURGE),
  MODERATOR_SHOW_DELETED_COMMENT("comment-moderator", AuthorizeAction.READ),
  MODERATOR_SPEAK("moderator-speak", AuthorizeAction.CREATE),

  // Admin permissions
  ADMIN_SHOW_DELETED_COMMENT("comment-admin", AuthorizeAction.READ),
  ADMIN_SPEAK("admin-speak", AuthorizeAction.CREATE);

  public final String Entity;
  public final AuthorizeAction Action;

  RolePermissionCommentTypes(String Entity, AuthorizeAction Action) {

    this.Entity = Entity;
    this.Action = Action;
  }

  @Override
  public String toString() {

    return this.Entity + ":" + this.Action;
  }
}
