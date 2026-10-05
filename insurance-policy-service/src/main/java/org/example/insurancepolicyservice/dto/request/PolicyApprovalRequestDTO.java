package org.example.insurancepolicyservice.dto.request;

// Carries the final proposal values selected by the Underwriter.
// The values may follow the system recommendation or contain a justified override.

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PolicyApprovalRequestDTO(

        @NotNull(message = "Proposed premium is required")
        @DecimalMin(
                value = "0.01",
                message = "Proposed premium must be greater than zero"
        )
        BigDecimal proposedPremium,

        @NotNull(message = "Proposed coverage limit is required")
        @DecimalMin(
                value = "0.01",
                message = "Proposed coverage limit must be greater than zero"
        )
        BigDecimal proposedCoverageLimit,

        @NotNull(message = "Proposed deductible is required")
        @DecimalMin(
                value = "0.00",
                message = "Proposed deductible cannot be negative"
        )
        BigDecimal proposedDeductible,

        @NotBlank(message = "Approval reason is required")
        @Size(
                min = 5,
                max = 500,
                message = "Approval reason must contain 5 to 500 characters"
        )
        String reason

) {
}