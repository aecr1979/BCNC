package com.bcnc.ecomerce.catalog.config;

public record AuthCredentialJsonDto(
    String clientKey,
    String username,
    String password,
    boolean enabled
) {
}

