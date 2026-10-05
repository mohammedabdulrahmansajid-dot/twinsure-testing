package org.example.insurancepolicyservice.dto.response;

// Returns the system-calculated underwriting result.
// The Underwriter uses this recommendation to make the final human decision.

import org.example.insurancepolicyservice.enums.RiskLevel;
import org.example.insurancepolicyservice.enums.UnderwritingRecommendation;

import java.math.BigDecimal;

public record RiskAssessmentResponseDTO(

        Long applicationId,
        int riskScore,
        RiskLevel riskLevel,
        BigDecimal riskMultiplier,
        BigDecimal basePremium,
        BigDecimal calculatedPremium,
        BigDecimal recommendedCoverageLimit,
        BigDecimal recommendedDeductible,
        UnderwritingRecommendation recommendation,
        String explanation

) {
}