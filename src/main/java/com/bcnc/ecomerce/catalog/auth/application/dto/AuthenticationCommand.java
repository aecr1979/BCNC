package com.bcnc.ecomerce.catalog.auth.application.dto;

public record AuthenticationCommand(
    String clientKey,
    String username,
    String password
) {
}

