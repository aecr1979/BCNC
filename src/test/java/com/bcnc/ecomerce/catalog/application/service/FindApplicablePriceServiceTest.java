package com.bcnc.ecomerce.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bcnc.ecomerce.catalog.application.port.out.FindApplicablePricePort;
import com.bcnc.ecomerce.catalog.domain.exception.DomainException;
import com.bcnc.ecomerce.catalog.domain.exception.PriceNotFoundException;
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
class FindApplicablePriceServiceTest {

    @Mock
    private FindApplicablePricePort findApplicablePricePort;

    private FindApplicablePriceService service;

    @BeforeEach
    void setUp() {
        service = new FindApplicablePriceService(findApplicablePricePort);
    }

    @Test
    void shouldReturnApplicablePriceWhenCandidatesExist() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 16, 0);
        Price highestPriority = price(2L, 1, new BigDecimal("25.45"));

        when(findApplicablePricePort.findApplicablePrice(1L, 35455L, applicationDate))
            .thenReturn(Optional.of(highestPriority));

        Price result = service.findApplicablePrice(applicationDate, 35455L, 1L);

        assertThat(result.priceList()).isEqualTo(2L);
        assertThat(result.priority()).isEqualTo(1);
        assertThat(result.price()).isEqualByComparingTo("25.45");
        verify(findApplicablePricePort).findApplicablePrice(1L, 35455L, applicationDate);
    }

    @Test
    void shouldReturnHighestPriorityPriceWhenMultipleCandidatesExist() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 16, 0);
        Price highest = price(4L, 2, new BigDecimal("20.00"));

        when(findApplicablePricePort.findApplicablePrice(1L, 35455L, applicationDate))
            .thenReturn(Optional.of(highest));

        Price result = service.findApplicablePrice(applicationDate, 35455L, 1L);

        assertThat(result.priceList()).isEqualTo(4L);
        assertThat(result.priority()).isEqualTo(2);
    }

    @Test
    void shouldThrowPriceNotFoundWhenNoCandidatesExist() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        when(findApplicablePricePort.findApplicablePrice(1L, 35455L, applicationDate)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findApplicablePrice(applicationDate, 35455L, 1L))
            .isInstanceOf(PriceNotFoundException.class)
            .hasMessage("No applicable price found for productId=35455, chainId=1, applicationDate=2020-06-14T10:00");
    }

    @Test
    void shouldThrowDomainExceptionWhenApplicationDateIsNull() {
        assertThatThrownBy(() -> service.findApplicablePrice(null, 35455L, 1L))
            .isInstanceOf(DomainException.class)
            .hasMessage("applicationDate must not be null");
    }

    @Test
    void shouldThrowDomainExceptionWhenProductIdIsNull() {
        assertThatThrownBy(() -> service.findApplicablePrice(LocalDateTime.now(), null, 1L))
            .isInstanceOf(DomainException.class)
            .hasMessage("productId must be greater than zero");
    }

    @Test
    void shouldThrowDomainExceptionWhenProductIdIsZeroOrLess() {
        assertThatThrownBy(() -> service.findApplicablePrice(LocalDateTime.now(), 0L, 1L))
            .isInstanceOf(DomainException.class)
            .hasMessage("productId must be greater than zero");

        assertThatThrownBy(() -> service.findApplicablePrice(LocalDateTime.now(), -1L, 1L))
            .isInstanceOf(DomainException.class)
            .hasMessage("productId must be greater than zero");
    }

    @Test
    void shouldThrowDomainExceptionWhenChainIdIsNull() {
        assertThatThrownBy(() -> service.findApplicablePrice(LocalDateTime.now(), 35455L, null))
            .isInstanceOf(DomainException.class)
            .hasMessage("chainId must be greater than zero");
    }

    @Test
    void shouldThrowDomainExceptionWhenChainIdIsZeroOrLess() {
        assertThatThrownBy(() -> service.findApplicablePrice(LocalDateTime.now(), 35455L, 0L))
            .isInstanceOf(DomainException.class)
            .hasMessage("chainId must be greater than zero");

        assertThatThrownBy(() -> service.findApplicablePrice(LocalDateTime.now(), 35455L, -1L))
            .isInstanceOf(DomainException.class)
            .hasMessage("chainId must be greater than zero");
    }

    private Price price(Long priceList, int priority, BigDecimal price) {
        return new Price(
            1L,
            LocalDateTime.of(2020, 6, 14, 0, 0),
            LocalDateTime.of(2020, 12, 31, 23, 59, 59),
            priceList,
            35455L,
            priority,
            price,
            "EUR"
        );
    }
}

