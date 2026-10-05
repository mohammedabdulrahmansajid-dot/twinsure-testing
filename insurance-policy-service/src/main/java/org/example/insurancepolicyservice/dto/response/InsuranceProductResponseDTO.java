package org.example.insurancepolicyservice.dto.response;

import org.example.insurancepolicyservice.enums.ProductStatus;

import java.math.BigDecimal;

public record InsuranceProductResponseDTO(

        Long productId,
        String productCode,
        String productName,
        String description,
        BigDecimal basePremium,
        BigDecimal coverageLimit,
        BigDecimal deductible,
        ProductStatus status

) {
}