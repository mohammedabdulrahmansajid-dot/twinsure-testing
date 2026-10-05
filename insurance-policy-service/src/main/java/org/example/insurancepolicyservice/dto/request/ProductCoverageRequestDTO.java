package org.example.insurancepolicyservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.insurancepolicyservice.enums.ActionType;
import org.example.insurancepolicyservice.enums.ViolationType;

import java.math.BigDecimal;

public record ProductCoverageRequestDTO(

        @NotNull(message = "Action type is required")
        ActionType actionType,

        @NotNull(message = "Violation type is required")
        ViolationType violationType,

        @DecimalMin(
                value = "0.01",
                message = "Coverage limit must be greater than zero"
        )
        BigDecimal coverageLimit,

        @NotBlank(message = "Coverage description is required")
        @Size(
                max = 500,
                message = "Coverage description must not exceed 500 characters"
        )
        String description

) {
}