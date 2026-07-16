package com.bcnc.ecomerce.catalog.adapter.out.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PersistenceEntitiesTest {

    @Test
    void shouldCreateAndMutatePriceEntity() {
        PriceEntity entity = new PriceEntity();
        LocalDateTime startDate = LocalDateTime.of(2020, 6, 14, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

        entity.setId(1L);
        entity.setBrandId(1L);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setPriceList(1L);
        entity.setProductId(35455L);
        entity.setPriority(0);
        entity.setPrice(new BigDecimal("35.50"));
        entity.setCurrency("EUR");

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getBrandId()).isEqualTo(1L);
        assertThat(entity.getStartDate()).isEqualTo(startDate);
        assertThat(entity.getEndDate()).isEqualTo(endDate);
        assertThat(entity.getPriceList()).isEqualTo(1L);
        assertThat(entity.getProductId()).isEqualTo(35455L);
        assertThat(entity.getPriority()).isEqualTo(0);
        assertThat(entity.getPrice()).isEqualByComparingTo("35.50");
        assertThat(entity.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void shouldCreatePriceEntityThroughConstructor() {
        LocalDateTime startDate = LocalDateTime.of(2020, 6, 14, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

        PriceEntity entity = new PriceEntity(1L, startDate, endDate, 1L, 35455L, 0, new BigDecimal("35.50"), "EUR");

        assertThat(entity.getBrandId()).isEqualTo(1L);
        assertThat(entity.getProductId()).isEqualTo(35455L);
        assertThat(entity.getPriceList()).isEqualTo(1L);
    }

    @Test
    void shouldCreateAndMutateAuthCredentialEntity() {
        AuthCredentialEntity entity = new AuthCredentialEntity();
        entity.setId(1L);
        entity.setClientKey("BCNC-CLIENT");
        entity.setUsername("catalog-user");
        entity.setPassword("encoded-password");
        entity.setEnabled(true);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getClientKey()).isEqualTo("BCNC-CLIENT");
        assertThat(entity.getUsername()).isEqualTo("catalog-user");
        assertThat(entity.getPassword()).isEqualTo("encoded-password");
        assertThat(entity.isEnabled()).isTrue();
    }
}

