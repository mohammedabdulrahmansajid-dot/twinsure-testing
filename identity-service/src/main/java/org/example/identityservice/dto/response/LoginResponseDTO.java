package org.example.identityservice.dto.response;

import org.example.identityservice.enums.Role;

public record LoginResponseDTO(

        Long userId,
        String username,
        Role role,
        Long customerId,
        String message

) {
}