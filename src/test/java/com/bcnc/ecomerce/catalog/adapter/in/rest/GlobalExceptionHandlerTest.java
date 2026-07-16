package com.bcnc.ecomerce.catalog.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bcnc.ecomerce.catalog.adapter.in.rest.api.model.ErrorResponse;
import com.bcnc.ecomerce.catalog.auth.domain.exception.AuthenticationFailedException;
import com.bcnc.ecomerce.catalog.domain.exception.DomainException;
import com.bcnc.ecomerce.catalog.domain.exception.PriceNotFoundException;
import jakarta.validation.Path;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.MethodParameter;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandlePriceNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handlePriceNotFound(new PriceNotFoundException("missing price"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("PRICE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("missing price");
    }

    @Test
    void shouldHandleAuthenticationFailed() {
        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationFailed(new AuthenticationFailedException("bad auth"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isEqualTo("AUTHENTICATION_FAILED");
    }

    @Test
    void shouldHandleDomainException() {
        ResponseEntity<ErrorResponse> response = handler.handleDomain(new DomainException("domain error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("DOMAIN_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("domain error");
    }

    @Test
    void shouldHandleMissingParameter() {
        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(
            new MissingServletRequestParameterException("chainId", "Long")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("MISSING_PARAMETER");
        assertThat(response.getBody().getMessage()).contains("chainId");
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "field", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).contains("field: must not be blank");
    }

    @Test
    void shouldHandleConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(
            new ConstraintViolationException(Set.of(violation))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).contains("field: must not be null");
    }

    @Test
    void shouldHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
            "bad-date", String.class, "applicationDate", (MethodParameter) null, new IllegalArgumentException()
        );

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_PARAMETER");
        assertThat(response.getBody().getMessage()).contains("applicationDate").contains("bad-date");
    }

    @Test
    void shouldHandleUnexpectedException() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
}

