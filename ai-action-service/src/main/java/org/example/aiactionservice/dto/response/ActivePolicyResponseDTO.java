package org.example.aiactionservice.dto.response;

// Represents the active-policy lookup response received from Insurance Service.
// It tells the evaluation engine whether the AI Twin had active insurance.

import java.time.LocalDate;

public record ActivePolicyResponseDTO(

        Long policyId,
        Long customerId,
        Long twinId,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        boolean active

) {
}