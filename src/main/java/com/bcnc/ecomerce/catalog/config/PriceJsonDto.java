package com.bcnc.ecomerce.catalog.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceJsonDto(
    Long brandId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Long priceList,
    Long productId,
    Integer priority,
    BigDecimal price,
    String currency
) {
}
