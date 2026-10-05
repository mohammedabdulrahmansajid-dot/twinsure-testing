package org.example.aitwinservice.dto.response;

import org.example.aitwinservice.enums.ActionType;
import org.example.aitwinservice.enums.PermissionLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TwinPermissionResponseDTO(

        Long permissionId,
        Long twinId,
        ActionType actionType,
        PermissionLevel permissionLevel,
        BigDecimal actionLimit,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}