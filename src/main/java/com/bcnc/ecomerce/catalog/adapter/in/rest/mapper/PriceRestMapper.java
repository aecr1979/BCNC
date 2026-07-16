package com.bcnc.ecomerce.catalog.adapter.in.rest.mapper;

import com.bcnc.ecomerce.catalog.adapter.in.rest.api.model.PriceResponse;
import com.bcnc.ecomerce.catalog.domain.model.Price;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class PriceRestMapper {

    public PriceResponse toResponse(Price price) {
        return new PriceResponse()
            .productId(price.productId())
            .chainId(price.brandId())
            .priceList(price.priceList())
            .startDate(toOffset(price.startDate()))
            .endDate(toOffset(price.endDate()))
            .price(price.price().doubleValue())
            .currency(price.currency());
    }

    private java.time.OffsetDateTime toOffset(java.time.LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.UTC);
    }
}
