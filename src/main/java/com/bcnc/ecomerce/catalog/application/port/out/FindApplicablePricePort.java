package com.bcnc.ecomerce.catalog.application.port.out;

import com.bcnc.ecomerce.catalog.domain.model.Price;
import java.time.LocalDateTime;
import java.util.Optional;

public interface FindApplicablePricePort {

    Optional<Price> findApplicablePrice(Long chainId, Long productId, LocalDateTime applicationDate);
}

