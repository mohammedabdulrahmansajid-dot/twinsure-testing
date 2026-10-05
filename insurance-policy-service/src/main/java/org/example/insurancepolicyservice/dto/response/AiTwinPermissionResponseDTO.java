package org.example.insurancepolicyservice.dto.response;

// Represents one AI Twin permission received from AI Twin Service.
// The underwriting calculator uses permission level and action limit as risk factors.

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AiTwinPermissionResponseDTO(

        Long permissionId,
        Long twinId,
        String actionType,
        String permissionLevel,
        BigDecimal actionLimit,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}