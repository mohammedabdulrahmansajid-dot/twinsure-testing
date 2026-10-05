package org.example.identityservice.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.identityservice.enums.UserStatus;

public record UserStatusRequestDTO(

        @NotNull(message = "User status is required")
        UserStatus status

) {
}