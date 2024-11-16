package com.sublinks.sublinksapi.api.sublinks.v1.privatemessage.models;

public record CreatePrivateMessage(
    String recipientKey,
    String message) {

}
