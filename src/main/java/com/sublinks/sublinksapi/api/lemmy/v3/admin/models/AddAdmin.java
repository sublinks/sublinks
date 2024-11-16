package com.sublinks.sublinksapi.api.lemmy.v3.admin.models;

import lombok.Builder;

/**
 * This class represents a request to add an admin to a site.
 */
@Builder
@SuppressWarnings("RecordComponentName")
public record AddAdmin(
    Integer person_id,
    Boolean added) {

}