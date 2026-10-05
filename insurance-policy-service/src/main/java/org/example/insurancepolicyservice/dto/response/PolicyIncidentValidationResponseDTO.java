package org.example.insurancepolicyservice.dto.response;

// Validates whether a policy covered a Customer and AI Twin on an action date.
// Claims Service uses this result before accepting an insured incident.

import org.example.insurancepolicyservice.enums.PolicyStatus;

import java.time.LocalDate;

public record PolicyIncidentValidationResponseDTO(

        Long policyId,
        Long customerId,
        Long twinId,
        PolicyStatus status,
        LocalDate policyStartDate,
        LocalDate policyEndDate,
        LocalDate actionDate,
        boolean customerMatches,
        boolean twinMatches,
        boolean activeOnActionDate,
        boolean valid

) {
}