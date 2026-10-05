package org.example.claimsservice.dto.response;

// Represents the complete policy coverage contract received from Insurance Service.
// It provides policy dates, financial limits, deductible, coverage rules,
// exclusions, and the policy's current validity.

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PolicyCoverageResponseDTO(

        Long policyId,
        Long customerId,
        Long twinId,
        Long productId,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal coverageLimit,
        BigDecimal deductible,
        List<PolicyCoverageRuleResponseDTO> coverages,
        List<PolicyExclusionResponseDTO> exclusions,
        boolean valid

) {
}