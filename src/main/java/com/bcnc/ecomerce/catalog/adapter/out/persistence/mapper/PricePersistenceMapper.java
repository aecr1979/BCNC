package com.bcnc.ecomerce.catalog.adapter.out.persistence.mapper;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.PriceEntity;
import com.bcnc.ecomerce.catalog.domain.model.Price;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PricePersistenceMapper {

    public Price toDomain(PriceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Price(
            entity.getBrandId(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getPriceList(),
            entity.getProductId(),
            entity.getPriority(),
            entity.getPrice(),
            entity.getCurrency()
        );
    }

    public List<Price> toDomainList(List<PriceEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
            .map(this::toDomain)
            .toList();
    }
}
