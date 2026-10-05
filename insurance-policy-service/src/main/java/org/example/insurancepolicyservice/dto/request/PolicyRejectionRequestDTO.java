package org.example.insurancepolicyservice.dto.request;

// Carries the mandatory reason when an Underwriter rejects an application.
// The reason is stored for auditing and displayed to the Customer.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PolicyRejectionRequestDTO(

        @NotBlank(message = "Rejection reason is required")
        @Size(
                min = 5,
                max = 500,
                message = "Rejection reason must contain 5 to 500 characters"
        )
        String reason

) {
}
