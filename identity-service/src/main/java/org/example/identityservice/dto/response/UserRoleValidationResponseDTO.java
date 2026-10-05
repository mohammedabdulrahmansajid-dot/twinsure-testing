package org.example.identityservice.dto.response;

import org.example.identityservice.enums.Role;
import org.example.identityservice.enums.UserStatus;

public record UserRoleValidationResponseDTO(

        Long userId,
        String username,
        Role role,
        UserStatus status,
        boolean valid

) {
}