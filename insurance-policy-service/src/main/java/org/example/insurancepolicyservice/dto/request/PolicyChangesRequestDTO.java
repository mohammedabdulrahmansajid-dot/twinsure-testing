package org.example.insurancepolicyservice.dto.request;

// Carries the Underwriter's explanation when application changes are required.
// The message tells the Customer what must be corrected before reassessment.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PolicyChangesRequestDTO(

        @NotBlank(message = "Change request reason is required")
        @Size(
                min = 5,
                max = 500,
                message = "Change request reason must contain 5 to 500 characters"
        )
        String reason

) {
}