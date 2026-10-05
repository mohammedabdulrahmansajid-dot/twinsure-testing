package org.example.claimsservice.dto.response;

// Represents a formal insurance claim in Customer and staff lists.
// It contains ownership, financial values, workflow status,
// assigned Adjuster, and important timestamps.

import org.example.claimsservice.enums.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClaimResponseDTO(

        Long claimId,
        String claimNumber,
        Long incidentId,
        Long customerId,
        Long twinId,
        Long policyId,
        Long actionId,
        BigDecimal claimedAmount,
        BigDecimal approvedAmount,
        BigDecimal deductibleApplied,
        ClaimStatus status,
        Long assignedAdjusterId,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt

) {
}