package com.bcnc.ecomerce.catalog.adapter.in.rest;

import com.bcnc.ecomerce.catalog.adapter.in.rest.api.PricesApi;
import com.bcnc.ecomerce.catalog.adapter.in.rest.api.model.PriceResponse;
import com.bcnc.ecomerce.catalog.adapter.in.rest.mapper.PriceRestMapper;
import com.bcnc.ecomerce.catalog.application.port.in.FindApplicablePriceUseCase;
import com.bcnc.ecomerce.catalog.domain.model.Price;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class PriceController implements PricesApi {

    private final FindApplicablePriceUseCase findApplicablePriceUseCase;
    private final PriceRestMapper priceRestMapper;

    public PriceController(FindApplicablePriceUseCase findApplicablePriceUseCase,
                           PriceRestMapper priceRestMapper) {
        this.findApplicablePriceUseCase = findApplicablePriceUseCase;
        this.priceRestMapper = priceRestMapper;
    }

    @Override
    @Operation(
        summary = "Get the applicable price for a product, chain and date",
        description = "Returns the price that must be applied for the given product identifier, " +
            "commercial chain identifier and application date. If no applicable rate is found, a 404 is returned.",
        tags = {"Prices"}
    )
    @ApiResponse(responseCode = "200", description = "Applicable price found")
    @ApiResponse(responseCode = "400", description = "Invalid or missing query parameters")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "404", description = "No applicable price found")
    public ResponseEntity<PriceResponse> getApplicablePrice(
            @Parameter(
                description = "Application (effective) date and time, in RFC 3339 format.",
                example = "2020-06-14T10:00:00Z",
                schema = @Schema(type = "string", format = "date-time", defaultValue = "2020-06-14T10:00:00Z")
            )
            OffsetDateTime applicationDate,
            @Parameter(
                description = "Product identifier.",
                example = "35455",
                schema = @Schema(type = "integer", format = "int64", defaultValue = "35455", minimum = "1")
            )
            Long productId,
            @Parameter(
                description = "Commercial chain (cadena / brand) identifier.",
                example = "1",
                schema = @Schema(type = "integer", format = "int64", defaultValue = "1", minimum = "1")
            )
            Long chainId) {

        Price price = findApplicablePriceUseCase.findApplicablePrice(
            applicationDate.toLocalDateTime(),
            productId,
            chainId
        );

        return ResponseEntity.ok(priceRestMapper.toResponse(price));
    }
}
