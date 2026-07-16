package com.bcnc.ecomerce.catalog.adapter.in.rest;
import com.bcnc.ecomerce.catalog.auth.application.port.out.TokenProviderPort;
import com.bcnc.ecomerce.catalog.auth.domain.exception.AuthenticationFailedException;
import com.bcnc.ecomerce.catalog.auth.domain.model.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private final TokenProviderPort tokenProviderPort;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    public BearerTokenAuthenticationFilter(TokenProviderPort tokenProviderPort,
                                           AuthenticationEntryPoint authenticationEntryPoint) {
        this.tokenProviderPort = tokenProviderPort;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
                throw new BadCredentialsException("A valid bearer token is required to access this resource");
            }
            String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
            AuthenticatedUser authenticatedUser = tokenProviderPort.validateAccessToken(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authenticatedUser.username(),
                token,
                List.of(new SimpleGrantedAuthority("ROLE_API_CONSUMER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (BadCredentialsException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, ex);
        } catch (AuthenticationFailedException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new BadCredentialsException(ex.getMessage(), ex));
        }
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/prices");
    }
}
