package org.example.claimsservice.dto.request;

// Carries the incident and requested compensation amount for a formal claim.
// Customer ownership is obtained from the JWT and verified by Claims Service.

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateClaimRequestDTO(

        @NotNull(message = "Incident ID is required")
        Long incidentId,

        @NotNull(message = "Claimed amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Claimed amount must be greater than zero"
        )
        BigDecimal claimedAmount

) {
}