package com.bcnc.ecomerce.catalog.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "catalog.security.auth")
public record AuthProperties(
    @NotBlank String tokenSecret,
    @Min(1) long tokenValidityMinutes,
    @Min(1) long refreshTokenValidityMinutes
) {
}
