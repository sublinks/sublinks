package com.sublinks.sublinksapi.api.sublinks.v1.comment.models;

import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record CommentResponse(
    @Schema(description = "The key of the comment",
        requiredMode = RequiredMode.REQUIRED) String key,
    @Schema(description = "The activity pub id of the comment",
        requiredMode = RequiredMode.REQUIRED) String activityPubId,
    @Schema(description = "The body of the comment",
        requiredMode = RequiredMode.REQUIRED) String body,
    @Schema(description = "The path of the comment",
        requiredMode = RequiredMode.REQUIRED) String path,
    @Schema(description = "Is the comment local",
        requiredMode = RequiredMode.REQUIRED) Boolean isLocal,
    @Schema(description = "Is the comment deleted",
        requiredMode = RequiredMode.REQUIRED) Boolean isDeleted,
    @Schema(description = "Is the comment featured",
        requiredMode = RequiredMode.REQUIRED) Boolean isFeatured,
    @Schema(description = "Is the comment removed",
        requiredMode = RequiredMode.REQUIRED) Boolean isRemoved,

    @Schema(description = "The creator of the comment",
        requiredMode = RequiredMode.REQUIRED) PersonResponse creator,
    @Schema(description = "The parent of the comment",
        requiredMode = RequiredMode.NOT_REQUIRED) List<CommentResponse> replies,
    @Schema(description = "The created at date",
        requiredMode = RequiredMode.REQUIRED) String createdAt,
    @Schema(description = "The updated at date",
        requiredMode = RequiredMode.REQUIRED) String updatedAt) {


  public String getId() {

    List<String> ids = List.of(key.split("\\."));
    return ids.get(ids.size() - 1);
  }

  public String getParentKey() {

    List<String> ids = List.of(key.split("\\."));
    return ids.size() > 1 ? String.join(".", ids.subList(0, ids.size() - 1)) : null;
  }
}
