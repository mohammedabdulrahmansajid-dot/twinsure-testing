package org.example.identityservice.dto.response;

import org.example.identityservice.enums.Role;
import org.example.identityservice.enums.UserStatus;

import java.time.LocalDateTime;

public record UserProfileResponseDTO(

        Long userId,
        String username,
        Role role,
        Long customerId,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}