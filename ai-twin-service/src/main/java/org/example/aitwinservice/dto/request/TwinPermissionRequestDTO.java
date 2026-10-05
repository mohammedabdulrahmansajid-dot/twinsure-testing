package org.example.aitwinservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.example.aitwinservice.enums.ActionType;
import org.example.aitwinservice.enums.PermissionLevel;

import java.math.BigDecimal;

public record TwinPermissionRequestDTO(

        @NotNull(message = "Action type is required")
        ActionType actionType,

        @NotNull(message = "Permission level is required")
        PermissionLevel permissionLevel,

        @DecimalMin(
                value = "0.01",
                message = "Action limit must be greater than zero"
        )
        BigDecimal actionLimit

) {
}