package com.sublinks.sublinksapi.api.sublinks.v1.instance.models;

import com.sublinks.sublinksapi.api.sublinks.v1.instance.enums.ListingType;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.enums.RegistrationMode;

public record UpdateInstanceConfig(
    RegistrationMode registrationMode,
    String registrationQuestion,
    Boolean privateInstance,
    Boolean requireEmailVerification,
    Boolean enableDownvotes,
    Boolean enableNsfw,
    Boolean communityCreationAdminOnly,
    Boolean applicationEmailAdmins,
    Boolean reportEmailAdmins,
    Boolean hideModlogModNames,
    Boolean federationEnabled,
    Boolean captchaEnabled,
    String captchaDifficulty,
    Integer nameMaxLength,
    String defaultTheme,
    ListingType defaultPostListingType,
    String legalInformation,
    String createdAt,
    String updatedAt) {

}
