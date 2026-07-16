package com.bcnc.ecomerce.catalog.application.port.in;

import com.bcnc.ecomerce.catalog.domain.model.Price;
import java.time.LocalDateTime;

public interface FindApplicablePriceUseCase {

    /**
     * Returns the price that must be applied for the given product, commercial chain
     * (brand) and application date. When several rates overlap, the one with the
     * highest priority is selected.
     *
     * @param applicationDate effective date/time of the price query
     * @param productId       product identifier
     * @param chainId         commercial chain (brand) identifier
     * @return the applicable {@link Price}
     * @throws com.bcnc.ecomerce.catalog.domain.exception.PriceNotFoundException
     *          when no rate applies to the given criteria
     */
    Price findApplicablePrice(LocalDateTime applicationDate, Long productId, Long chainId);
}
