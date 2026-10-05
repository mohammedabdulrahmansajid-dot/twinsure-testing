package org.example.insurancepolicyservice.dto.response;

// Provides a complete issued-policy contract and underwriting snapshot.
// Coverage rules, exclusions, and captured AI Twin configuration
// remain available for Customer and staff policy review.

import org.example.insurancepolicyservice.enums.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PolicyDetailsResponseDTO(

        Long policyId,
        String policyNumber,
        Long applicationId,
        Long customerId,
        Long twinId,
        Long productId,

        String productCode,
        String productName,
        String productDescription,

        BigDecimal premium,
        BigDecimal coverageLimit,
        BigDecimal deductible,

        LocalDate startDate,
        LocalDate endDate,
        PolicyStatus status,

        String cancellationReason,

        List<ProductCoverageResponseDTO> coverages,
        List<ProductExclusionResponseDTO> exclusions,

        PolicyTwinSnapshotResponseDTO twinSnapshot,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}