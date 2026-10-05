package org.example.aiactionservice.dto.response;

// Represents one permission returned by AI Twin Service.
// The evaluation engine uses the permission level and action limit
// to determine whether a simulated action follows the Twin's rules.

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