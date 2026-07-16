package com.bcnc.ecomerce.catalog.adapter.out.security;
import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationResult;
import com.bcnc.ecomerce.catalog.auth.application.port.out.TokenProviderPort;
import com.bcnc.ecomerce.catalog.auth.domain.exception.AuthenticationFailedException;
import com.bcnc.ecomerce.catalog.auth.domain.model.AuthenticatedUser;
import com.bcnc.ecomerce.catalog.config.AuthProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
@Component
public class HmacTokenProvider implements TokenProviderPort {
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final String TOKEN_TYPE = "Bearer";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String ACCESS = "ACCESS";
    private static final String REFRESH = "REFRESH";
    private final AuthProperties authProperties;
    public HmacTokenProvider(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }
    @Override
    public AuthenticationResult generateTokenPair(AuthenticatedUser user) {
        Instant accessExpiration = Instant.now().plusSeconds(authProperties.tokenValidityMinutes() * 60);
        Instant refreshExpiration = Instant.now().plusSeconds(authProperties.refreshTokenValidityMinutes() * 60);
        String accessToken = buildToken(ACCESS, user, accessExpiration);
        String refreshToken = buildToken(REFRESH, user, refreshExpiration);
        return new AuthenticationResult(
            accessToken,
            refreshToken,
            TOKEN_TYPE,
            OffsetDateTime.ofInstant(accessExpiration, ZoneOffset.UTC),
            OffsetDateTime.ofInstant(refreshExpiration, ZoneOffset.UTC)
        );
    }
    @Override
    public AuthenticatedUser validateAccessToken(String token) {
        ParsedToken parsedToken = validateToken(token, ACCESS, "A valid bearer token is required to access this resource");
        return new AuthenticatedUser(parsedToken.clientKey(), parsedToken.username());
    }
    @Override
    public AuthenticationResult refreshAccessToken(String refreshToken) {
        ParsedToken parsedToken = validateToken(refreshToken, REFRESH, "Invalid refresh token");
        return generateTokenPair(new AuthenticatedUser(parsedToken.clientKey(), parsedToken.username()));
    }
    private String buildToken(String tokenKind, AuthenticatedUser user, Instant expiration) {
        String payload = "%s|%s|%s|%s".formatted(tokenKind, user.username(), user.clientKey(), expiration.getEpochSecond());
        String encodedPayload = BASE64_URL_ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = BASE64_URL_ENCODER.encodeToString(sign(payload));
        return encodedPayload + "." + signature;
    }
    private ParsedToken validateToken(String token, String expectedKind, String blankTokenMessage) {
        if (token == null || token.isBlank()) {
            throw new AuthenticationFailedException(blankTokenMessage);
        }
        String[] segments = token.split("\\.");
        if (segments.length != 2) {
            throw new AuthenticationFailedException(expectedKind.equals(REFRESH) ? "Invalid refresh token" : "Invalid bearer token");
        }
        String payload = decodePayload(segments[0], expectedKind);
        validateSignature(payload, segments[1], expectedKind);
        String[] values = payload.split("\\|");
        if (values.length != 4) {
            throw new AuthenticationFailedException(expectedKind.equals(REFRESH)
                ? "Invalid refresh token"
                : "Invalid bearer token payload");
        }
        if (!expectedKind.equals(values[0])) {
            throw new AuthenticationFailedException(expectedKind.equals(REFRESH)
                ? "Invalid refresh token"
                : "Invalid bearer token");
        }
        long expirationEpochSeconds = parseExpiration(values[3], expectedKind);
        if (Instant.now().isAfter(Instant.ofEpochSecond(expirationEpochSeconds))) {
            throw new AuthenticationFailedException(expectedKind.equals(REFRESH)
                ? "Refresh token has expired"
                : "Bearer token has expired");
        }
        return new ParsedToken(values[1], values[2]);
    }
    private String decodePayload(String encodedPayload, String tokenKind) {
        try {
            return new String(BASE64_URL_DECODER.decode(encodedPayload), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new AuthenticationFailedException(tokenKind.equals(REFRESH) ? "Invalid refresh token" : "Invalid bearer token");
        }
    }
    private void validateSignature(String payload, String providedSignature, String tokenKind) {
        String expectedSignature = BASE64_URL_ENCODER.encodeToString(sign(payload));
        boolean matches = MessageDigest.isEqual(
            expectedSignature.getBytes(StandardCharsets.UTF_8),
            providedSignature.getBytes(StandardCharsets.UTF_8)
        );
        if (!matches) {
            throw new AuthenticationFailedException(tokenKind.equals(REFRESH)
                ? "Invalid refresh token"
                : "Invalid bearer token signature");
        }
    }
    private long parseExpiration(String value, String tokenKind) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new AuthenticationFailedException(tokenKind.equals(REFRESH)
                ? "Invalid refresh token"
                : "Invalid bearer token expiration");
        }
    }
    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(authProperties.tokenSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new AuthenticationFailedException("Unable to generate authentication token");
        }
    }
    private record ParsedToken(String username, String clientKey) {
    }
}
