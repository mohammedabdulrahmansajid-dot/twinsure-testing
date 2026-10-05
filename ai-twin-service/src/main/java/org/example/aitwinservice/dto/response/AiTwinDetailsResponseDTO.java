package org.example.aitwinservice.dto.response;

import org.example.aitwinservice.enums.AiTwinStatus;
import org.example.aitwinservice.enums.AutonomyLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AiTwinDetailsResponseDTO(

        Long twinId,
        Long customerId,
        String twinName,
        String providerName,
        String modelName,
        AutonomyLevel autonomyLevel,
        BigDecimal transactionLimit,
        BigDecimal approvalThreshold,
        AiTwinStatus status,
        List<TwinPermissionResponseDTO> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}