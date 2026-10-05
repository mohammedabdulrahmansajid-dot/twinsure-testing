package org.example.claimsservice.dto.response;

// Represents policy validation received from Insurance Policy Service.
// It confirms whether the policy covered the Customer and AI Twin
// on the date when the AI action occurred.

import java.time.LocalDate;

public record PolicyIncidentValidationResponseDTO(

        Long policyId,
        Long customerId,
        Long twinId,
        String status,
        LocalDate policyStartDate,
        LocalDate policyEndDate,
        LocalDate actionDate,
        boolean customerMatches,
        boolean twinMatches,
        boolean activeOnActionDate,
        boolean valid

) {
}