package org.example.aiactionservice.dto.response;

// Represents one rule violation detected during action evaluation.
// It tells the caller what failed, its severity, and whether the
// violation is potentially eligible for a claim.

import org.example.aiactionservice.enums.ViolationSeverity;
import org.example.aiactionservice.enums.ViolationType;

import java.time.LocalDateTime;

public record ActionViolationResponseDTO(

        Long violationId,
        Long actionId,
        ViolationType violationType,
        ViolationSeverity severity,
        String description,
        Boolean claimEligible,
        LocalDateTime createdAt

) {
}