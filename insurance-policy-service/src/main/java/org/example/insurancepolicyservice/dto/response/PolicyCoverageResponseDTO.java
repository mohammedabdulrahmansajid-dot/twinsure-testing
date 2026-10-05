package org.example.insurancepolicyservice.dto.response;

// Supplies policy coverage information to Claims Service.
// It includes the limits, deductible, coverages, exclusions, and policy validity.

import org.example.insurancepolicyservice.enums.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PolicyCoverageResponseDTO(

        Long policyId,
        Long customerId,
        Long twinId,
        Long productId,
        PolicyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal coverageLimit,
        BigDecimal deductible,
        List<ProductCoverageResponseDTO> coverages,
        List<ProductExclusionResponseDTO> exclusions,
        boolean valid

) {
}