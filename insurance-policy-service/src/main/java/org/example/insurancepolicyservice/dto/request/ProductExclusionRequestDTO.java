package org.example.insurancepolicyservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductExclusionRequestDTO(

        @NotBlank(message = "Exclusion code is required")
        @Size(
                min = 3,
                max = 100,
                message = "Exclusion code must contain 3 to 100 characters"
        )
        String exclusionCode,

        @NotBlank(message = "Exclusion description is required")
        @Size(
                max = 500,
                message = "Exclusion description must not exceed 500 characters"
        )
        String description

) {
}