package org.example.identityservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.identityservice.enums.Role;

public record CreateStaffUserRequestDTO(

        @NotBlank(message = "Username is required")
        @Size(
                min = 4,
                max = 50,
                message = "Username must contain 4 to 50 characters"
        )
        String username,

        @NotBlank(message = "Password is required")
        @Size(
                min = 6,
                max = 100,
                message = "Password must contain at least 6 characters"
        )
        String password,

        @NotNull(message = "Role is required")
        Role role

) {
}