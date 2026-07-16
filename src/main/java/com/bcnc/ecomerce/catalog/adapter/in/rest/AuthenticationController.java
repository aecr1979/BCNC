package com.bcnc.ecomerce.catalog.adapter.in.rest;

import com.bcnc.ecomerce.catalog.adapter.in.rest.api.AuthenticationApi;
import com.bcnc.ecomerce.catalog.adapter.in.rest.api.model.RefreshTokenRequest;
import com.bcnc.ecomerce.catalog.adapter.in.rest.api.model.TokenRequest;
import com.bcnc.ecomerce.catalog.adapter.in.rest.api.model.TokenResponse;
import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationCommand;
import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationResult;
import com.bcnc.ecomerce.catalog.auth.application.dto.RefreshTokenCommand;
import com.bcnc.ecomerce.catalog.auth.application.port.in.AuthenticateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication", description = "Token generation operations")
public class AuthenticationController implements AuthenticationApi {
    private final AuthenticateUseCase authenticateUseCase;

    public AuthenticationController(AuthenticateUseCase authenticateUseCase) {
        this.authenticateUseCase = authenticateUseCase;
    }

    @Override
    @Operation(
        summary = "Generate an access token for the API",
        description = "Authenticates a consumer using a simple client text string (clientKey), " +
            "username and password, and returns a bearer token that must be sent in the " +
            "Authorization header to access protected endpoints.",
        tags = {"Authentication"}
    )
    @ApiResponse(responseCode = "200", description = "Access token generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid authentication request")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<TokenResponse> generateAccessToken(TokenRequest tokenRequest) {
        AuthenticationResult result = authenticateUseCase.authenticate(new AuthenticationCommand(
            tokenRequest.getClientKey(),
            tokenRequest.getUsername(),
            tokenRequest.getPassword()
        ));
        return ResponseEntity.ok(toResponse(result));
    }

    @Override
    @Operation(
        summary = "Refresh the API access token",
        description = "Receives a valid refresh token and returns a new bearer token pair. " +
            "Refresh tokens are intended only for token renewal and must not be used " +
            "to access protected business endpoints.",
        tags = {"Authentication"}
    )
    @ApiResponse(responseCode = "200", description = "Access token refreshed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid refresh token request")
    @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    public ResponseEntity<TokenResponse> refreshAccessToken(RefreshTokenRequest refreshTokenRequest) {
        AuthenticationResult result = authenticateUseCase.refreshAccessToken(
            new RefreshTokenCommand(refreshTokenRequest.getRefreshToken())
        );
        return ResponseEntity.ok(toResponse(result));
    }

    private TokenResponse toResponse(AuthenticationResult result) {
        return new TokenResponse()
            .accessToken(result.accessToken())
            .refreshToken(result.refreshToken())
            .tokenType(result.tokenType())
            .expiresAt(result.expiresAt())
            .refreshExpiresAt(result.refreshExpiresAt());
    }
}
