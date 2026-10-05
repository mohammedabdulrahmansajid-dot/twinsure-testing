package org.example.insurancepolicyservice.dto.response;

// Represents the Customer validation response received from Customer Service.
// Insurance Service uses it before accepting a policy application.

public record CustomerValidationResponseDTO(

        Long customerId,
        Long userId,
        String status,
        boolean valid

) {
}