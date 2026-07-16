package com.bcnc.ecomerce.catalog.auth.application.dto;

import java.time.OffsetDateTime;

public record AuthenticationResult(
    String accessToken,
    String refreshToken,
    String tokenType,
    OffsetDateTime expiresAt,
    OffsetDateTime refreshExpiresAt
) {
}

