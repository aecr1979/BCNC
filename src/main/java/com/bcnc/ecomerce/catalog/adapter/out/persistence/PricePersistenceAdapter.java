package com.bcnc.ecomerce.catalog.adapter.out.persistence;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.mapper.PricePersistenceMapper;
import com.bcnc.ecomerce.catalog.adapter.out.persistence.repository.PriceRepository;
import com.bcnc.ecomerce.catalog.application.port.out.FindApplicablePricePort;
import com.bcnc.ecomerce.catalog.domain.model.Price;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PricePersistenceAdapter implements FindApplicablePricePort {

    private final PriceRepository priceRepository;
    private final PricePersistenceMapper pricePersistenceMapper;

    public PricePersistenceAdapter(PriceRepository priceRepository,
                                   PricePersistenceMapper pricePersistenceMapper) {
        this.priceRepository = priceRepository;
        this.pricePersistenceMapper = pricePersistenceMapper;
    }

    @Override
    public Optional<Price> findApplicablePrice(Long chainId, Long productId, LocalDateTime applicationDate) {
        return priceRepository
            .findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                chainId,
                productId,
                applicationDate,
                applicationDate
            )
            .map(pricePersistenceMapper::toDomain);
    }
}

