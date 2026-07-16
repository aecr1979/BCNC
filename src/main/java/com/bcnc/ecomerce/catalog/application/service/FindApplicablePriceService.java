package com.bcnc.ecomerce.catalog.application.service;

import com.bcnc.ecomerce.catalog.application.port.in.FindApplicablePriceUseCase;
import com.bcnc.ecomerce.catalog.application.port.out.FindApplicablePricePort;
import com.bcnc.ecomerce.catalog.domain.exception.DomainException;
import com.bcnc.ecomerce.catalog.domain.exception.PriceNotFoundException;
import com.bcnc.ecomerce.catalog.domain.model.Price;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class FindApplicablePriceService implements FindApplicablePriceUseCase {

    private final FindApplicablePricePort findApplicablePricePort;

    public FindApplicablePriceService(FindApplicablePricePort findApplicablePricePort) {
        this.findApplicablePricePort = findApplicablePricePort;
    }

    @Override
    public Price findApplicablePrice(LocalDateTime applicationDate, Long productId, Long chainId) {
        validateInput(applicationDate, productId, chainId);

        Price price = findApplicablePricePort.findApplicablePrice(chainId, productId, applicationDate)
            .orElseThrow(() -> new PriceNotFoundException(
                "No applicable price found for productId=%s, chainId=%s, applicationDate=%s"
                    .formatted(productId, chainId, applicationDate)
            ));

        return price;
    }

    private void validateInput(LocalDateTime applicationDate, Long productId, Long chainId) {
        if (applicationDate == null) {
            throw new DomainException("applicationDate must not be null");
        }
        if (productId == null || productId <= 0) {
            throw new DomainException("productId must be greater than zero");
        }
        if (chainId == null || chainId <= 0) {
            throw new DomainException("chainId must be greater than zero");
        }
    }
}
