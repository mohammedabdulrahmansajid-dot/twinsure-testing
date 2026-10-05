package org.example.claimsservice.dto.response;

// Represents one rule violation received from AI Action Service.
// Claims Service uses the violation type, severity, and eligibility
// information when validating an incident and evaluating a claim.

import java.time.LocalDateTime;

public record ActionViolationEvidenceResponseDTO(

        Long violationId,
        Long actionId,
        String violationType,
        String severity,
        String description,
        Boolean claimEligible,
        LocalDateTime createdAt

) {
}