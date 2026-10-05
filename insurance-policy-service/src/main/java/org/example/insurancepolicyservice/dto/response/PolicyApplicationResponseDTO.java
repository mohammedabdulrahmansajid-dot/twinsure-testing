package org.example.insurancepolicyservice.dto.response;

// Represents a policy application in Customer and Underwriter lists.
// It contains application status, risk result, proposal, and review information.

import org.example.insurancepolicyservice.enums.PolicyApplicationStatus;
import org.example.insurancepolicyservice.enums.RiskLevel;
import org.example.insurancepolicyservice.enums.UnderwritingRecommendation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PolicyApplicationResponseDTO(

        Long applicationId,
        Long customerId,
        Long twinId,
        Long productId,
        PolicyApplicationStatus status,
        Integer riskScore,
        RiskLevel riskLevel,
        UnderwritingRecommendation systemRecommendation,
        BigDecimal proposedPremium,
        BigDecimal proposedCoverageLimit,
        BigDecimal proposedDeductible,
        Long reviewedBy,
        String decisionReason,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        LocalDateTime proposalExpiresAt,
        LocalDateTime acceptedAt

) {
}