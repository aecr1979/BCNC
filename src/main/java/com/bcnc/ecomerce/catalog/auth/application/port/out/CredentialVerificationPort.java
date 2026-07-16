package com.bcnc.ecomerce.catalog.auth.application.port.out;

import com.bcnc.ecomerce.catalog.auth.domain.model.AuthenticatedUser;
import java.util.Optional;

public interface CredentialVerificationPort {

    Optional<AuthenticatedUser> verify(String clientKey, String username, String password);
}

