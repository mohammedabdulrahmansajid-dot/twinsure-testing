package org.example.claimsservice.dto.request;

// Carries the Claims Adjuster user ID selected for claim assignment.
// An Admin uses this request to send a submitted claim to an Adjuster.

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignClaimRequestDTO(

        @NotNull(message = "Claims Adjuster user ID is required")
        @Positive(message = "Claims Adjuster user ID must be positive")
        Long adjusterId

) {
}