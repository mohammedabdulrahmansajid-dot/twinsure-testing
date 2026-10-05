package org.example.claimsservice.dto.response;

// Represents Identity Service user-role validation.
// Claims Service uses this contract to reject invalid claim assignees.

public record UserRoleValidationResponseDTO(

        Long userId,
        String username,
        String role,
        String status,
        boolean valid

) {
}