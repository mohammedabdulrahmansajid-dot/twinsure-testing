package org.example.insurancepolicyservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateInsuranceProductRequestDTO(

        @NotBlank(message = "Product name is required")
        @Size(
                min = 3,
                max = 150,
                message = "Product name must contain 3 to 150 characters"
        )
        String productName,

        @NotBlank(message = "Description is required")
        @Size(
                min = 10,
                max = 500,
                message = "Description must contain 10 to 500 characters"
        )
        String description,

        @NotNull(message = "Base premium is required")
        @DecimalMin(
                value = "0.01",
                message = "Base premium must be greater than zero"
        )
        BigDecimal basePremium,

        @NotNull(message = "Coverage limit is required")
        @DecimalMin(
                value = "0.01",
                message = "Coverage limit must be greater than zero"
        )
        BigDecimal coverageLimit,

        @NotNull(message = "Deductible is required")
        @DecimalMin(
                value = "0.00",
                message = "Deductible cannot be negative"
        )
        BigDecimal deductible

) {
}