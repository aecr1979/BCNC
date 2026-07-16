package com.bcnc.ecomerce.catalog.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bcnc.ecomerce.catalog.auth.application.port.out.TokenProviderPort;
import com.bcnc.ecomerce.catalog.auth.domain.exception.AuthenticationFailedException;
import com.bcnc.ecomerce.catalog.auth.domain.model.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

@ExtendWith(MockitoExtension.class)
class BearerTokenAuthenticationFilterTest {

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private TestableBearerTokenAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TestableBearerTokenAuthenticationFilter(tokenProviderPort, authenticationEntryPoint);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateRequestWhenBearerTokenIsValid() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
        when(tokenProviderPort.validateAccessToken("valid-token"))
            .thenReturn(new AuthenticatedUser("BCNC-CLIENT", "catalog-user"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("catalog-user");
        verify(filterChain).doFilter(request, response);
        verify(authenticationEntryPoint, never()).commence(any(), any(), any());
    }

    @Test
    void shouldDelegateToEntryPointWhenAuthorizationHeaderIsMissing() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(authenticationEntryPoint).commence(eq(request), eq(response), any(BadCredentialsException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldDelegateToEntryPointWhenAuthorizationHeaderDoesNotContainBearerPrefix() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationEntryPoint).commence(eq(request), eq(response), any(BadCredentialsException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldDelegateToEntryPointWhenTokenValidationFails() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalid-token");
        when(tokenProviderPort.validateAccessToken("invalid-token"))
            .thenThrow(new AuthenticationFailedException("Invalid bearer token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationEntryPoint).commence(eq(request), eq(response), any(BadCredentialsException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldFilterOnlyPriceEndpoint() {
        HttpServletRequest pricesRequest = mock(HttpServletRequest.class);
        HttpServletRequest authRequest = mock(HttpServletRequest.class);
        when(pricesRequest.getRequestURI()).thenReturn("/api/v1/prices");
        when(authRequest.getRequestURI()).thenReturn("/api/v1/auth/token");

        assertThat(filter.exposesShouldNotFilter(pricesRequest)).isFalse();
        assertThat(filter.exposesShouldNotFilter(authRequest)).isTrue();
    }

    private static final class TestableBearerTokenAuthenticationFilter extends BearerTokenAuthenticationFilter {

        private TestableBearerTokenAuthenticationFilter(TokenProviderPort tokenProviderPort,
                                                        AuthenticationEntryPoint authenticationEntryPoint) {
            super(tokenProviderPort, authenticationEntryPoint);
        }

        private boolean exposesShouldNotFilter(HttpServletRequest request) {
            return super.shouldNotFilter(request);
        }
    }
}

