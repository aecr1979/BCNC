package com.bcnc.ecomerce.catalog.auth.application.service;

import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationCommand;
import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationResult;
import com.bcnc.ecomerce.catalog.auth.application.dto.RefreshTokenCommand;
import com.bcnc.ecomerce.catalog.auth.application.port.in.AuthenticateUseCase;
import com.bcnc.ecomerce.catalog.auth.application.port.out.CredentialVerificationPort;
import com.bcnc.ecomerce.catalog.auth.application.port.out.TokenProviderPort;
import com.bcnc.ecomerce.catalog.auth.domain.exception.AuthenticationFailedException;
import com.bcnc.ecomerce.catalog.auth.domain.model.AuthenticatedUser;
import com.bcnc.ecomerce.catalog.domain.exception.DomainException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements AuthenticateUseCase {

    private final CredentialVerificationPort credentialVerificationPort;
    private final TokenProviderPort tokenProviderPort;

    public AuthenticationService(CredentialVerificationPort credentialVerificationPort,
                                 TokenProviderPort tokenProviderPort) {
        this.credentialVerificationPort = credentialVerificationPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public AuthenticationResult authenticate(AuthenticationCommand command) {
        validate(command);

        AuthenticatedUser authenticatedUser = credentialVerificationPort
            .verify(command.clientKey(), command.username(), command.password())
            .orElseThrow(() -> new AuthenticationFailedException("Invalid authentication credentials"));

        return tokenProviderPort.generateTokenPair(authenticatedUser);
    }

    @Override
    public AuthenticationResult refreshAccessToken(RefreshTokenCommand command) {
        if (command == null || isBlank(command.refreshToken())) {
            throw new DomainException("refreshToken must not be blank");
        }
        return tokenProviderPort.refreshAccessToken(command.refreshToken());
    }

    private void validate(AuthenticationCommand command) {
        if (command == null) {
            throw new DomainException("authentication command must not be null");
        }
        if (isBlank(command.clientKey())) {
            throw new DomainException("clientKey must not be blank");
        }
        if (isBlank(command.username())) {
            throw new DomainException("username must not be blank");
        }
        if (isBlank(command.password())) {
            throw new DomainException("password must not be blank");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

