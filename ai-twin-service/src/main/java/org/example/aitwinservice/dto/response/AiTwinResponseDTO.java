package org.example.aitwinservice.dto.response;

import org.example.aitwinservice.enums.AiTwinStatus;
import org.example.aitwinservice.enums.AutonomyLevel;

import java.math.BigDecimal;

public record AiTwinResponseDTO(

        Long twinId,
        Long customerId,
        String twinName,
        String providerName,
        String modelName,
        AutonomyLevel autonomyLevel,
        BigDecimal transactionLimit,
        BigDecimal approvalThreshold,
        AiTwinStatus status

) {
}