package org.example.aiactionservice.dto.response;

// Represents the complete action-rule contract returned by AI Twin Service.
// It provides ownership, status, financial limits, autonomy, and permissions
// required to evaluate a simulated action.

import java.math.BigDecimal;
import java.util.List;

public record AiTwinActionRulesResponseDTO(

        Long twinId,
        Long customerId,
        String status,
        String autonomyLevel,
        BigDecimal transactionLimit,
        BigDecimal approvalThreshold,
        List<AiTwinPermissionResponseDTO> permissions,
        boolean valid

) {
}