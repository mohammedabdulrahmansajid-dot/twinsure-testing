package org.example.aitwinservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.example.aitwinservice.enums.AutonomyLevel;

import java.math.BigDecimal;

public record UpdateAiTwinRequestDTO(

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