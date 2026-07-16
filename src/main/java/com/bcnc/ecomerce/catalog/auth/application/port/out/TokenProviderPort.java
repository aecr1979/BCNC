package com.bcnc.ecomerce.catalog.auth.application.port.out;

import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationResult;
import com.bcnc.ecomerce.catalog.auth.domain.model.AuthenticatedUser;

public interface TokenProviderPort {

    AuthenticationResult generateTokenPair(AuthenticatedUser user);

    AuthenticatedUser validateAccessToken(String token);

    AuthenticationResult refreshAccessToken(String refreshToken);
}

