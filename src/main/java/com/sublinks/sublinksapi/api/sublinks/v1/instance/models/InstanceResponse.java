package com.sublinks.sublinksapi.api.sublinks.v1.instance.models;

public record InstanceResponse(
    String key,
    String name,
    String description,
    String domain,
    String software,
    String version,
    String sidebar,
    String iconUrl,
    String bannerUrl,
    String publicKey,
    String createdAt,
    String updatedAt) {

}
