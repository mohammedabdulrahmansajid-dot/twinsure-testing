package org.example.insurancepolicyservice.dto.response;

// Represents an issued insurance policy in lists and standard responses.
// It contains the final premium, coverage period, limits, current status,
// and the cancellation reason when the policy is cancelled.

import org.example.insurancepolicyservice.enums.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyResponseDTO(

        Long policyId,
        String policyNumber,
        Long applicationId,
        Long customerId,
        Long twinId,
        Long productId,
        BigDecimal premium,
        BigDecimal coverageLimit,
        BigDecimal deductible,
        LocalDate startDate,
        LocalDate endDate,
        PolicyStatus status,
        String cancellationReason

) {
}