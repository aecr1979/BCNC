package com.bcnc.ecomerce.catalog.domain.model;

public record AuthCredential(
    Long id,
    String clientKey,
    String username,
    String password,
    boolean enabled
) {
}
