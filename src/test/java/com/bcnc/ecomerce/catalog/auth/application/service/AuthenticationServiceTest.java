package com.bcnc.ecomerce.catalog.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationCommand;
import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationResult;
import com.bcnc.ecomerce.catalog.auth.application.dto.RefreshTokenCommand;
import com.bcnc.ecomerce.catalog.auth.application.port.out.CredentialVerificationPort;
import com.bcnc.ecomerce.catalog.auth.application.port.out.TokenProviderPort;
import com.bcnc.ecomerce.catalog.auth.domain.exception.AuthenticationFailedException;
import com.bcnc.ecomerce.catalog.auth.domain.model.AuthenticatedUser;
import com.bcnc.ecomerce.catalog.domain.exception.DomainException;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private CredentialVerificationPort credentialVerificationPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationService(credentialVerificationPort, tokenProviderPort);
    }

    @Test
    void shouldAuthenticateSuccessfullyAndReturnTokenPair() {
        AuthenticationCommand command = new AuthenticationCommand("BCNC-CLIENT", "catalog-user", "catalog-password");
        AuthenticatedUser user = new AuthenticatedUser("BCNC-CLIENT", "catalog-user");
        AuthenticationResult result = authResult();

        when(credentialVerificationPort.verify("BCNC-CLIENT", "catalog-user", "catalog-password"))
            .thenReturn(Optional.of(user));
        when(tokenProviderPort.generateTokenPair(user)).thenReturn(result);

        AuthenticationResult actual = service.authenticate(command);

        assertThat(actual).isEqualTo(result);
        verify(credentialVerificationPort).verify("BCNC-CLIENT", "catalog-user", "catalog-password");
        verify(tokenProviderPort).generateTokenPair(user);
    }

    @Test
    void shouldThrowAuthenticationFailedWhenCredentialsAreInvalid() {
        AuthenticationCommand command = new AuthenticationCommand("BCNC-CLIENT", "catalog-user", "bad-password");
        when(credentialVerificationPort.verify("BCNC-CLIENT", "catalog-user", "bad-password"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(command))
            .isInstanceOf(AuthenticationFailedException.class)
            .hasMessage("Invalid authentication credentials");
    }

    @Test
    void shouldThrowDomainExceptionWhenCommandIsNull() {
        assertThatThrownBy(() -> service.authenticate(null))
            .isInstanceOf(DomainException.class)
            .hasMessage("authentication command must not be null");
    }

    @Test
    void shouldThrowDomainExceptionWhenClientKeyIsBlank() {
        assertThatThrownBy(() -> service.authenticate(new AuthenticationCommand(" ", "user", "password")))
            .isInstanceOf(DomainException.class)
            .hasMessage("clientKey must not be blank");
    }

    @Test
    void shouldThrowDomainExceptionWhenUsernameIsBlank() {
        assertThatThrownBy(() -> service.authenticate(new AuthenticationCommand("BCNC", "", "password")))
            .isInstanceOf(DomainException.class)
            .hasMessage("username must not be blank");
    }

    @Test
    void shouldThrowDomainExceptionWhenPasswordIsBlank() {
        assertThatThrownBy(() -> service.authenticate(new AuthenticationCommand("BCNC", "user", "   ")))
            .isInstanceOf(DomainException.class)
            .hasMessage("password must not be blank");
    }

    @Test
    void shouldRefreshAccessTokenSuccessfully() {
        AuthenticationResult result = authResult();
        when(tokenProviderPort.refreshAccessToken("refresh-token")).thenReturn(result);

        AuthenticationResult actual = service.refreshAccessToken(new RefreshTokenCommand("refresh-token"));

        assertThat(actual).isEqualTo(result);
        verify(tokenProviderPort).refreshAccessToken("refresh-token");
    }

    @Test
    void shouldThrowDomainExceptionWhenRefreshCommandIsNull() {
        assertThatThrownBy(() -> service.refreshAccessToken(null))
            .isInstanceOf(DomainException.class)
            .hasMessage("refreshToken must not be blank");
    }

    @Test
    void shouldThrowDomainExceptionWhenRefreshTokenIsBlank() {
        assertThatThrownBy(() -> service.refreshAccessToken(new RefreshTokenCommand("  ")))
            .isInstanceOf(DomainException.class)
            .hasMessage("refreshToken must not be blank");
    }

    private AuthenticationResult authResult() {
        return new AuthenticationResult(
            "access-token",
            "refresh-token",
            "Bearer",
            OffsetDateTime.parse("2030-06-14T12:00:00Z"),
            OffsetDateTime.parse("2030-06-21T12:00:00Z")
        );
    }
}

