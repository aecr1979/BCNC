package com.bcnc.ecomerce.catalog.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

class RestAuthenticationEntryPointTest {

    @Test
    void shouldWriteUnauthorizedJsonResponseWithProvidedMessage() throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(new ObjectMapper());
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
            new MockHttpServletRequest(),
            response,
            new BadCredentialsException("Invalid bearer token")
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED").contains("Invalid bearer token");
    }

    @Test
    void shouldWriteDefaultUnauthorizedMessageWhenExceptionMessageIsBlank() throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(new ObjectMapper());
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
            new MockHttpServletRequest(),
            response,
            new BadCredentialsException(" ")
        );

        assertThat(response.getContentAsString()).contains("A valid bearer token is required to access this resource");
    }
}

