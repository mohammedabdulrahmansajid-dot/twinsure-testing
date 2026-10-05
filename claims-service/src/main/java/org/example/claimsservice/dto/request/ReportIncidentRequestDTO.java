package org.example.claimsservice.dto.request;

// Carries the AI action, policy, incident type, and reported financial loss.
// Customer identity and ownership are taken securely from the JWT and
// validated against the connected TwinSure services.

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.claimsservice.enums.IncidentType;

import java.math.BigDecimal;

public record ReportIncidentRequestDTO(

        @NotNull(message = "AI action ID is required")
        Long actionId,

        @NotNull(message = "Policy ID is required")
        Long policyId,

        @NotNull(message = "Incident type is required")
        IncidentType incidentType,

        @NotBlank(message = "Incident description is required")
        @Size(
                min = 10,
                max = 500,
                message = "Description must contain 10 to 500 characters"
        )
        String description,

        @NotNull(message = "Loss amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Loss amount must be greater than zero"
        )
        BigDecimal lossAmount

) {
}