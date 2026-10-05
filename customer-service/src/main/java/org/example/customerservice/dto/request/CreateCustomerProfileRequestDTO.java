package org.example.customerservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerProfileRequestDTO(

        @NotBlank(message = "Full name is required")
        @Size(
                min = 2,
                max = 150,
                message = "Full name must contain 2 to 150 characters"
        )
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(
                max = 150,
                message = "Email must not exceed 150 characters"
        )
        String email,

        @NotBlank(message = "Phone number is required")
        @Size(
                min = 10,
                max = 20,
                message = "Phone number must contain 10 to 20 characters"
        )
        String phone,

        @NotBlank(message = "Address is required")
        @Size(
                min = 5,
                max = 500,
                message = "Address must contain 5 to 500 characters"
        )
        String address

) {
}