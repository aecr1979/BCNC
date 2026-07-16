package com.bcnc.ecomerce.catalog.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.PriceEntity;
import com.bcnc.ecomerce.catalog.adapter.out.persistence.repository.PriceRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

@ExtendWith(MockitoExtension.class)
class PriceDataLoaderTest {

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldSkipLoadingWhenRepositoryAlreadyContainsData() throws Exception {
        when(priceRepository.count()).thenReturn(1L);

        PriceDataLoader loader = new PriceDataLoader();
        CommandLineRunner runner = loader.initPriceDatabase(priceRepository, objectMapper);

        runner.run();

        verify(priceRepository, never()).saveAll(any());
    }

    @Test
    void shouldLoadPricesWhenRepositoryIsEmpty() throws Exception {
        when(priceRepository.count()).thenReturn(0L);
        when(objectMapper.readValue(any(InputStream.class), org.mockito.ArgumentMatchers.<TypeReference<List<PriceJsonDto>>>any()))
            .thenReturn(List.of(new PriceJsonDto(
                1L,
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                1L,
                35455L,
                0,
                new BigDecimal("35.50"),
                "EUR"
            )));

        PriceDataLoader loader = new PriceDataLoader();
        CommandLineRunner runner = loader.initPriceDatabase(priceRepository, objectMapper);

        runner.run();

        verify(priceRepository).saveAll(any());
    }
}
