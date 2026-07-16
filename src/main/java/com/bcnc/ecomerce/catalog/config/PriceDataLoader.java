package com.bcnc.ecomerce.catalog.config;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.PriceEntity;
import com.bcnc.ecomerce.catalog.adapter.out.persistence.repository.PriceRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class PriceDataLoader {

    private static final Logger log = LoggerFactory.getLogger(PriceDataLoader.class);
    private static final String PRICES_JSON_PATH = "data/prices.json";

    @Bean
    CommandLineRunner initPriceDatabase(PriceRepository priceRepository,
                                        ObjectMapper objectMapper) {
        return args -> {
            if (priceRepository.count() == 0) {
                log.info("Loading initial price data from JSON...");
                ClassPathResource resource = new ClassPathResource(PRICES_JSON_PATH);
                try (InputStream inputStream = resource.getInputStream()) {
                    List<PriceJsonDto> priceDtos = objectMapper.readValue(
                        inputStream,
                        new TypeReference<List<PriceJsonDto>>() {}
                    );
                    List<PriceEntity> entities = priceDtos.stream()
                        .map(dto -> {
                            PriceEntity entity = new PriceEntity();
                            entity.setBrandId(dto.brandId());
                            entity.setStartDate(dto.startDate());
                            entity.setEndDate(dto.endDate());
                            entity.setPriceList(dto.priceList());
                            entity.setProductId(dto.productId());
                            entity.setPriority(dto.priority());
                            entity.setPrice(dto.price());
                            entity.setCurrency(dto.currency());
                            return entity;
                        })
                        .toList();
                    priceRepository.saveAll(entities);
                    log.info("Price data loaded successfully! Total records: {}", priceRepository.count());
                } catch (Exception e) {
                    log.error("Failed to load price data", e);
                    throw new RuntimeException("Failed to load price data", e);
                }
            }
        };
    }
}
