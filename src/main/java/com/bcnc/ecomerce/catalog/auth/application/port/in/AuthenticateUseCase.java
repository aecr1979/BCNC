package com.bcnc.ecomerce.catalog.auth.application.port.in;

import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationCommand;
import com.bcnc.ecomerce.catalog.auth.application.dto.AuthenticationResult;
import com.bcnc.ecomerce.catalog.auth.application.dto.RefreshTokenCommand;

public interface AuthenticateUseCase {

    AuthenticationResult authenticate(AuthenticationCommand command);

    AuthenticationResult refreshAccessToken(RefreshTokenCommand command);
}

