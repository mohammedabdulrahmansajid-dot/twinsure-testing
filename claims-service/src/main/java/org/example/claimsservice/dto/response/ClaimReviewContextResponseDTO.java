package org.example.claimsservice.dto.response;

// Provides the authoritative claim-review calculation used by Adjusters.
// It combines action evidence, historical policy validity, coverage limits,
// deductible calculations, exclusions, and the maximum payable amount.

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ClaimReviewContextResponseDTO(

        Long claimId,
        String claimNumber,
        Long incidentId,
        Long actionId,
        Long policyId,
        Long customerId,
        Long twinId,

        String actionType,
        String incidentType,
        String violationType,
        String violationSeverity,

        LocalDate actionDate,

        boolean actionViolationFound,
        boolean actionMarkedClaimEligible,

        boolean customerMatches,
        boolean twinMatches,
        boolean policyValidOnActionDate,

        boolean coverageMatched,

        boolean activeExclusionsPresent,
        boolean automaticExclusionDetected,
        boolean manualExclusionReviewRequired,

        BigDecimal claimedAmount,
        BigDecimal reportedLossAmount,

        BigDecimal overallPolicyCoverageLimit,
        BigDecimal applicableCoverageLimit,

        BigDecimal policyDeductible,
        BigDecimal deductibleApplied,

        BigDecimal grossEligibleAmount,
        BigDecimal maximumPayableAmount,

        boolean eligible,

        String explanation,

        List<PolicyCoverageRuleResponseDTO> matchingCoverages,
        List<PolicyExclusionResponseDTO> activeExclusions

) {
}