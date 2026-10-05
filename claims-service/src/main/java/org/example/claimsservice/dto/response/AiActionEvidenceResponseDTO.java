package org.example.claimsservice.dto.response;

// Represents the complete AI action evidence received from AI Action Service.
// Claims Service uses it to verify ownership, insurance, action status,
// action date, financial amount, and detected rule violations.

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AiActionEvidenceResponseDTO(

        Long actionId,
        Long customerId,
        Long twinId,
        Long policyId,
        String actionType,
        String transactionReference,
        String description,
        BigDecimal actionAmount,
        Boolean approvalProvided,
        String actionStatus,
        Boolean insured,
        List<ActionViolationEvidenceResponseDTO> violations,
        LocalDateTime occurredAt,
        LocalDateTime evaluatedAt,
        LocalDateTime createdAt

) {
}