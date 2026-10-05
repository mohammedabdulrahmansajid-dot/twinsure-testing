package org.example.insurancepolicyservice.dto.request;

// Carries an administrative policy-status change.
// It is used when an Admin cancels or changes a policy status.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.insurancepolicyservice.enums.PolicyStatus;

public record PolicyStatusRequestDTO(

        @NotNull(message = "Policy status is required")
        PolicyStatus status,

        @NotBlank(message = "Status change reason is required")
        @Size(
                min = 5,
                max = 500,
                message = "Reason must contain 5 to 500 characters"
        )
        String reason

) {
}