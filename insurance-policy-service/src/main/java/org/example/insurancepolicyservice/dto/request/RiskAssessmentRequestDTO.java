package org.example.insurancepolicyservice.dto.request;

// Carries additional information required during risk assessment.
// The system combines this information with the AI Twin risk profile.

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record RiskAssessmentRequestDTO(

        @Min(
                value = 0,
                message = "Previous incident count cannot be negative"
        )
        @Max(
                value = 100,
                message = "Previous incident count cannot exceed 100"
        )
        int previousIncidentCount,

        @Size(
                max = 500,
                message = "Assessment remarks must not exceed 500 characters"
        )
        String assessmentRemarks

) {
}