package org.example.aitwinservice.dto.response;

import org.example.aitwinservice.enums.AiTwinStatus;
import org.example.aitwinservice.enums.AutonomyLevel;

import java.math.BigDecimal;
import java.util.List;

public record AiTwinRiskProfileResponseDTO(

        Long twinId,
        Long customerId,
        String twinName,
        AutonomyLevel autonomyLevel,
        BigDecimal transactionLimit,
        BigDecimal approvalThreshold,
        AiTwinStatus status,
        List<TwinPermissionResponseDTO> permissions

) {
}