package org.example.insurancepolicyservice.dto.response;

// Represents the risk-profile response received from AI Twin Service.
// It provides the configuration used to calculate risk score and premium.

import java.math.BigDecimal;
import java.util.List;

public record AiTwinRiskProfileResponseDTO(

        Long twinId,
        Long customerId,
        String twinName,
        String autonomyLevel,
        BigDecimal transactionLimit,
        BigDecimal approvalThreshold,
        String status,
        List<AiTwinPermissionResponseDTO> permissions

) {
}