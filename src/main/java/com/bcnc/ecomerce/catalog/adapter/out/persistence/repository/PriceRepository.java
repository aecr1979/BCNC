package com.bcnc.ecomerce.catalog.adapter.out.persistence.repository;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.PriceEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<PriceEntity, Long> {

    Optional<PriceEntity> findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
        Long brandId,
        Long productId,
        LocalDateTime applicationDate,
        LocalDateTime applicationDateEnd
    );
}
