package com.bcnc.ecomerce.catalog.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "OpaqueToken",
    in = SecuritySchemeIn.HEADER,
    description = "Use the bearer token obtained from POST /api/v1/auth/token."
)
public class OpenApiSecurityConfiguration {
}

