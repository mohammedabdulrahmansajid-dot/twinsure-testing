package org.example.insurancepolicyservice.dto.response;

// Represents ownership and status validation received from AI Twin Service.
// It prevents a Customer from insuring another Customer's AI Twin.

public record AiTwinValidationResponseDTO(

        Long twinId,
        Long customerId,
        String status,
        boolean ownedByCustomer,
        boolean valid

) {
}