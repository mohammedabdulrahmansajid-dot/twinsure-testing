package org.example.aitwinservice.dto.response;

import org.example.aitwinservice.enums.AiTwinStatus;
import org.example.aitwinservice.enums.AutonomyLevel;

import java.math.BigDecimal;
import java.util.List;

public record AiTwinActionRulesResponseDTO(

        Long twinId,
        Long customerId,
        AiTwinStatus status,
        AutonomyLevel autonomyLevel,
        BigDecimal transactionLimit,
        BigDecimal approvalThreshold,
        List<TwinPermissionResponseDTO> permissions,
        boolean valid

) {
}