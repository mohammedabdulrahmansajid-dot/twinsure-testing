package org.example.insurancepolicyservice.dto.response;

import org.example.insurancepolicyservice.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InsuranceProductDetailsResponseDTO(

        Long productId,
        String productCode,
        String productName,
        String description,
        BigDecimal basePremium,
        BigDecimal coverageLimit,
        BigDecimal deductible,
        ProductStatus status,
        List<ProductCoverageResponseDTO> coverages,
        List<ProductExclusionResponseDTO> exclusions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}