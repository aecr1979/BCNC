package com.bcnc.ecomerce.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.PriceEntity;
import com.bcnc.ecomerce.catalog.adapter.out.persistence.mapper.PricePersistenceMapper;
import com.bcnc.ecomerce.catalog.adapter.out.persistence.repository.PriceRepository;
import com.bcnc.ecomerce.catalog.domain.model.Price;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PricePersistenceAdapterTest {

    @Mock
    private PriceRepository priceRepository;

    private PricePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PricePersistenceAdapter(priceRepository, new PricePersistenceMapper());
    }

    @Test
    void shouldReturnMappedApplicablePriceWhenRepositoryFindsOne() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 16, 0);
        PriceEntity entity = priceEntity(2L, 1, new BigDecimal("25.45"));

        when(priceRepository.findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
            1L,
            35455L,
            applicationDate,
            applicationDate
        )).thenReturn(Optional.of(entity));

        Optional<Price> result = adapter.findApplicablePrice(1L, 35455L, applicationDate);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().priceList()).isEqualTo(2L);
        assertThat(result.orElseThrow().priority()).isEqualTo(1);
        verify(priceRepository).findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
            1L,
            35455L,
            applicationDate,
            applicationDate
        );
    }

    @Test
    void shouldReturnEmptyWhenRepositoryDoesNotFindApplicablePrice() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);

        when(priceRepository.findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
            1L,
            35455L,
            applicationDate,
            applicationDate
        )).thenReturn(Optional.empty());

        Optional<Price> result = adapter.findApplicablePrice(1L, 35455L, applicationDate);

        assertThat(result).isEmpty();
    }

    private PriceEntity priceEntity(Long priceList, int priority, BigDecimal price) {
        PriceEntity entity = new PriceEntity();
        entity.setBrandId(1L);
        entity.setProductId(35455L);
        entity.setStartDate(LocalDateTime.of(2020, 6, 14, 0, 0));
        entity.setEndDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59));
        entity.setPriceList(priceList);
        entity.setPriority(priority);
        entity.setPrice(price);
        entity.setCurrency("EUR");
        return entity;
    }
}

