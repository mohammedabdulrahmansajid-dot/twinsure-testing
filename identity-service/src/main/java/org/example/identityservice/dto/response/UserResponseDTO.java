package org.example.identityservice.dto.response;

import org.example.identityservice.enums.Role;
import org.example.identityservice.enums.UserStatus;

public record UserResponseDTO(

        Long userId,
        String username,
        Role role,
        Long customerId,
        UserStatus status

) {
}
