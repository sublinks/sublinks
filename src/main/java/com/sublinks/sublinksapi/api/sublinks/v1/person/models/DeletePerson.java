package com.sublinks.sublinksapi.api.sublinks.v1.person.models;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record DeletePerson(
    @Schema(description = "The reason for deleting the person",
        example = "I dont use this account anymore",
        requiredMode = RequiredMode.NOT_REQUIRED) String reason,
    @Schema(description = "Whether to pin your Post/Comments/Private Messages or not",
        example = "true",
        defaultValue = "false",
        requiredMode = RequiredMode.NOT_REQUIRED) Boolean deleteContent) {

}
