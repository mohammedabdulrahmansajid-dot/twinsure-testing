package org.example.insurancepolicyservice.dto.request;

// Carries the AI Twin and insurance product selected by a Customer.
// The Customer ID is not accepted here because it comes securely from the JWT.

import jakarta.validation.constraints.NotNull;

public record CreatePolicyApplicationRequestDTO(

        @NotNull(message = "AI Twin ID is required")
        Long twinId,

        @NotNull(message = "Insurance product ID is required")
        Long productId

) {
}