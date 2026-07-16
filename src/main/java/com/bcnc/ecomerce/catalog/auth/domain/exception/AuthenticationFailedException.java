package com.bcnc.ecomerce.catalog.auth.domain.exception;

import com.bcnc.ecomerce.catalog.domain.exception.DomainException;

public class AuthenticationFailedException extends DomainException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}

