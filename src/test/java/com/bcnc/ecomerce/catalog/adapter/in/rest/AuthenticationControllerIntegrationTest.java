package com.bcnc.ecomerce.catalog.adapter.in.rest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    void shouldGenerateAccessAndRefreshTokensForValidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "clientKey": "BCNC-CLIENT",
                      "username": "catalog-user",
                      "password": "catalog-password"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresAt").exists())
            .andExpect(jsonPath("$.refreshExpiresAt").exists());
    }
    @Test
    void shouldRefreshTokenPairWithValidRefreshToken() throws Exception {
        String refreshToken = obtainTokenPair().get("refreshToken").asText();
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": "%s"
                    }
                    """.formatted(refreshToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresAt").exists())
            .andExpect(jsonPath("$.refreshExpiresAt").exists());
    }
    @Test
    void shouldReturnUnauthorizedForInvalidRefreshToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": "invalid-refresh-token"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"))
            .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "clientKey": "BCNC-CLIENT",
                      "username": "catalog-user",
                      "password": "wrong-password"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"))
            .andExpect(jsonPath("$.message").value("Invalid authentication credentials"));
    }
    @Test
    void shouldReturnUnauthorizedForMalformedBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                .header("Authorization", "Bearer malformed-token")
                .queryParam("applicationDate", "2020-06-14T10:00:00Z")
                .queryParam("productId", "35455")
                .queryParam("chainId", "1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
    @Test
    void shouldRejectRefreshTokenWhenUsedAsBearerToken() throws Exception {
        String refreshToken = obtainTokenPair().get("refreshToken").asText();
        mockMvc.perform(get("/api/v1/prices")
                .header("Authorization", "Bearer " + refreshToken)
                .queryParam("applicationDate", "2020-06-14T10:00:00Z")
                .queryParam("productId", "35455")
                .queryParam("chainId", "1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
    @Test
    void shouldExposeBearerSecuritySchemeInOpenApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
            .andExpect(jsonPath("$.paths['/api/v1/auth/refresh'].post.operationId").value("refreshAccessToken"));
    }
    private JsonNode obtainTokenPair() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "clientKey": "BCNC-CLIENT",
                      "username": "catalog-user",
                      "password": "catalog-password"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
