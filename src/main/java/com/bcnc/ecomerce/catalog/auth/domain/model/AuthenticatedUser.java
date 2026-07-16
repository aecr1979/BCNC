package com.bcnc.ecomerce.catalog.auth.domain.model;

/**
 * Compatibility model kept under the auth bounded context path so IDE/build
 * caches that still reference this location can resolve it safely.
 */
public record AuthenticatedUser(
    String clientKey,
    String username
) {
}

