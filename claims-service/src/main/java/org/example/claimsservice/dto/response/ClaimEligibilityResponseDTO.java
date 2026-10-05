package org.example.claimsservice.dto.response;

// Summarizes the automatic claim-eligibility checks performed by Claims Service.
// It explains whether ownership, policy dates, action evidence,
// coverage rules, and financial limits passed validation.

import java.math.BigDecimal;

public record ClaimEligibilityResponseDTO(

        Long incidentId,
        Long actionId,
        Long policyId,
        boolean actionViolationFound,
        boolean actionMarkedClaimEligible,
        boolean policyValid,
        boolean coverageMatched,
        boolean exclusionDetected,
        BigDecimal reportedLossAmount,
        BigDecimal applicableCoverageLimit,
        BigDecimal deductible,
        boolean eligible,
        String explanation

) {
}