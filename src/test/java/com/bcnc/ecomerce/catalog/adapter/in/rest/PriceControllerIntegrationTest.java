package com.bcnc.ecomerce.catalog.adapter.in.rest;

import static org.hamcrest.Matchers.closeTo;
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
class PriceControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    void shouldReturnApplicablePriceForTest1() throws Exception {
        assertApplicablePrice("2020-06-14T10:00:00Z", 1, 35.50, "2020-06-14T00:00:00Z", "2020-12-31T23:59:59Z");
    }

    @Test
    void shouldReturnApplicablePriceForTest2() throws Exception {
        assertApplicablePrice("2020-06-14T16:00:00Z", 2, 25.45, "2020-06-14T15:00:00Z", "2020-06-14T18:30:00Z");
    }

    @Test
    void shouldReturnApplicablePriceForTest3() throws Exception {
        assertApplicablePrice("2020-06-14T21:00:00Z", 1, 35.50, "2020-06-14T00:00:00Z", "2020-12-31T23:59:59Z");
    }

    @Test
    void shouldReturnApplicablePriceForTest4() throws Exception {
        assertApplicablePrice("2020-06-15T10:00:00Z", 3, 30.50, "2020-06-15T00:00:00Z", "2020-06-15T11:00:00Z");
    }

    @Test
    void shouldReturnApplicablePriceForTest5() throws Exception {
        assertApplicablePrice("2020-06-16T21:00:00Z", 4, 38.95, "2020-06-15T16:00:00Z", "2020-12-31T23:59:59Z");
    }
    @Test
    void shouldApplyHighestPriorityWhenTwoRatesOverlap() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvc.perform(get("/api/v1/prices")
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("applicationDate", "2020-06-14T16:00:00Z")
                .queryParam("productId", "35455")
                .queryParam("chainId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.priceList").value(2))
            .andExpect(jsonPath("$.price", closeTo(25.45, 0.001)));
    }
    @Test
    void shouldReturnNotFoundWhenNoApplicablePriceExists() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvc.perform(get("/api/v1/prices")
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("applicationDate", "2020-06-14T10:00:00Z")
                .queryParam("productId", "99999")
                .queryParam("chainId", "1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("PRICE_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value(
                "No applicable price found for productId=99999, chainId=1, applicationDate=2020-06-14T10:00"
            ));
    }
    @Test
    void shouldReturnBadRequestWhenApplicationDateHasInvalidFormat() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvc.perform(get("/api/v1/prices")
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("applicationDate", "invalid-date")
                .queryParam("productId", "35455")
                .queryParam("chainId", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
            .andExpect(jsonPath("$.message").value(
                "Parameter 'applicationDate' has an invalid value 'invalid-date'"
            ));
    }
    @Test
    void shouldReturnBadRequestWhenRequiredParameterIsMissing() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvc.perform(get("/api/v1/prices")
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("applicationDate", "2020-06-14T10:00:00Z")
                .queryParam("productId", "35455"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
            .andExpect(jsonPath("$.message").value("Required parameter 'chainId' is missing"));
    }
    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                .queryParam("applicationDate", "2020-06-14T10:00:00Z")
                .queryParam("productId", "35455")
                .queryParam("chainId", "1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
    @Test
    void shouldExposeDefaultExamplesInOpenApiDocsForSwaggerTryItOut() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/v1/prices'].get.parameters[0].example").value("2020-06-14T10:00:00Z"))
            .andExpect(jsonPath("$.paths['/api/v1/prices'].get.parameters[0].schema.default").value("2020-06-14T10:00:00Z"))
            .andExpect(jsonPath("$.paths['/api/v1/prices'].get.parameters[1].example").value("35455"))
            .andExpect(jsonPath("$.paths['/api/v1/prices'].get.parameters[1].schema.default").value("35455"))
            .andExpect(jsonPath("$.paths['/api/v1/prices'].get.parameters[2].example").value("1"))
            .andExpect(jsonPath("$.paths['/api/v1/prices'].get.parameters[2].schema.default").value("1"));
    }

    private void assertApplicablePrice(String applicationDate,
                                       long expectedPriceList,
                                       double expectedPrice,
                                       String expectedStartDate,
                                       String expectedEndDate) throws Exception {
        String accessToken = obtainAccessToken();

        mockMvc.perform(get("/api/v1/prices")
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("applicationDate", applicationDate)
                .queryParam("productId", "35455")
                .queryParam("chainId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(35455))
            .andExpect(jsonPath("$.chainId").value(1))
            .andExpect(jsonPath("$.priceList").value(expectedPriceList))
            .andExpect(jsonPath("$.price", closeTo(expectedPrice, 0.001)))
            .andExpect(jsonPath("$.currency").value("EUR"))
            .andExpect(jsonPath("$.startDate").value(expectedStartDate))
            .andExpect(jsonPath("$.endDate").value(expectedEndDate));
    }

    private String obtainAccessToken() throws Exception {
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
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("accessToken").asText();
    }
}
