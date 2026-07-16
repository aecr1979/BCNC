package com.bcnc.ecomerce.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PriceAndAuthCredentialModelTest {

    @Test
    void shouldCreatePriceModelThroughConstructor() {
        LocalDateTime startDate = LocalDateTime.of(2020, 6, 14, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

        Price price = new Price(1L, startDate, endDate, 1L, 35455L, 0, new BigDecimal("35.50"), "EUR");

        assertThat(price.brandId()).isEqualTo(1L);
        assertThat(price.startDate()).isEqualTo(startDate);
        assertThat(price.endDate()).isEqualTo(endDate);
        assertThat(price.priceList()).isEqualTo(1L);
        assertThat(price.productId()).isEqualTo(35455L);
        assertThat(price.priority()).isEqualTo(0);
        assertThat(price.price()).isEqualByComparingTo("35.50");
        assertThat(price.currency()).isEqualTo("EUR");
    }

    @Test
    void shouldCreateAuthCredentialThroughConstructor() {
        AuthCredential credential = new AuthCredential(2L, "CLIENT", "user", "secret", false);

        assertThat(credential.id()).isEqualTo(2L);
        assertThat(credential.clientKey()).isEqualTo("CLIENT");
        assertThat(credential.username()).isEqualTo("user");
        assertThat(credential.password()).isEqualTo("secret");
        assertThat(credential.enabled()).isFalse();
    }
}
