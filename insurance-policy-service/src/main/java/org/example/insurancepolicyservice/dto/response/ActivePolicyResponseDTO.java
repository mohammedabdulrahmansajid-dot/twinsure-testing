package org.example.insurancepolicyservice.dto.response;

// Indicates whether an AI Twin currently has active insurance.
// AI Action Service uses this result to determine incident eligibility.

import org.example.insurancepolicyservice.enums.PolicyStatus;

import java.time.LocalDate;

public record ActivePolicyResponseDTO(

        Long policyId,
        Long customerId,
        Long twinId,
        PolicyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        boolean active

) {
}