package com.bcnc.ecomerce.catalog.adapter.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.PriceEntity;
import com.bcnc.ecomerce.catalog.domain.model.Price;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricePersistenceMapperTest {

    private final PricePersistenceMapper mapper = new PricePersistenceMapper();

    @Test
    void shouldMapEntityToDomain() {
        PriceEntity entity = new PriceEntity();
        entity.setBrandId(1L);
        entity.setStartDate(LocalDateTime.of(2020, 6, 14, 0, 0));
        entity.setEndDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59));
        entity.setPriceList(1L);
        entity.setProductId(35455L);
        entity.setPriority(0);
        entity.setPrice(new BigDecimal("35.50"));
        entity.setCurrency("EUR");

        Price result = mapper.toDomain(entity);

        assertThat(result.brandId()).isEqualTo(1L);
        assertThat(result.productId()).isEqualTo(35455L);
        assertThat(result.priceList()).isEqualTo(1L);
        assertThat(result.priority()).isEqualTo(0);
        assertThat(result.price()).isEqualByComparingTo("35.50");
        assertThat(result.currency()).isEqualTo("EUR");
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void shouldMapEntityListToDomainList() {
        PriceEntity entity = new PriceEntity(1L,
            LocalDateTime.of(2020, 6, 14, 0, 0),
            LocalDateTime.of(2020, 12, 31, 23, 59, 59),
            1L,
            35455L,
            0,
            new BigDecimal("35.50"),
            "EUR");

        List<Price> result = mapper.toDomainList(List.of(entity));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().priceList()).isEqualTo(1L);
    }

    @Test
    void shouldReturnEmptyListWhenEntitiesAreNull() {
        assertThat(mapper.toDomainList(null)).isEmpty();
    }
}

