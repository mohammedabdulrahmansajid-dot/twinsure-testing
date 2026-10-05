package org.example.claimsservice.dto.request;

// Carries the approved compensation amount and Adjuster's decision reason.
// Claims Service validates the amount against the claimed amount,
// policy coverage limit, financial loss, and deductible.

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ApproveClaimRequestDTO(

        @NotNull(message = "Approved amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Approved amount must be greater than zero"
        )
        BigDecimal approvedAmount,

        @NotBlank(message = "Approval reason is required")
        @Size(
                min = 5,
                max = 500,
                message = "Approval reason must contain 5 to 500 characters"
        )
        String reason

) {
}
