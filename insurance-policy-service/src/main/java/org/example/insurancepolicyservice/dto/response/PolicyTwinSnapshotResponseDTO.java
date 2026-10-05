package org.example.insurancepolicyservice.dto.response;

// Represents the exact AI Twin risk configuration captured at issuance.
// Customers and staff can compare this snapshot with the policy terms.

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PolicyTwinSnapshotResponseDTO(

        Long twinId,
        String twinName,
        String autonomyLevel,
        BigDecimal transactionLimit,
        BigDecimal approvalThreshold,
        String twinStatus,
        LocalDateTime capturedAt,
        List<PolicyPermissionSnapshotResponseDTO> permissions

) {
}