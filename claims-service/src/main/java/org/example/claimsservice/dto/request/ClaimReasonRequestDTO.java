package org.example.claimsservice.dto.request;

// Carries a mandatory explanation for a claim workflow decision.
// It can be reused for starting review, requesting information,
// rejection, and claim closure operations.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClaimReasonRequestDTO(

        @NotBlank(message = "Decision reason is required")
        @Size(
                min = 5,
                max = 500,
                message = "Decision reason must contain 5 to 500 characters"
        )
        String reason

) {
}
