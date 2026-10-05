package org.example.claimsservice.dto.response;

// Represents one Claims Adjuster decision in the claim audit history.
// It shows who made the decision, the status transition,
// approved amount, reason, and decision time.

import org.example.claimsservice.enums.ClaimDecisionType;
import org.example.claimsservice.enums.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClaimDecisionResponseDTO(

        Long decisionId,
        Long claimId,
        Long adjusterId,
        ClaimDecisionType decisionType,
        ClaimStatus previousStatus,
        ClaimStatus newStatus,
        BigDecimal approvedAmount,
        String reason,
        LocalDateTime decidedAt

) {
}