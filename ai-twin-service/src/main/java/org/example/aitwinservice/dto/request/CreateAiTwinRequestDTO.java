package org.example.aitwinservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.aitwinservice.enums.AutonomyLevel;

import java.math.BigDecimal;

public record CreateAiTwinRequestDTO(

        @NotBlank(message = "Twin name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Twin name must contain 2 to 100 characters"
        )
        String twinName,

        @NotBlank(message = "Provider name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Provider name must contain 2 to 100 characters"
        )
        String providerName,

        @NotBlank(message = "Model name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Model name must contain 2 to 100 characters"
        )
        String modelName,

        @NotNull(message = "Autonomy level is required")
        AutonomyLevel autonomyLevel,

        @NotNull(message = "Transaction limit is required")
        @DecimalMin(
                value = "0.01",
                message = "Transaction limit must be greater than zero"
        )
        BigDecimal transactionLimit,

        @NotNull(message = "Approval threshold is required")
        @DecimalMin(
                value = "0.00",
                message = "Approval threshold cannot be negative"
        )
        BigDecimal approvalThreshold

) {
}