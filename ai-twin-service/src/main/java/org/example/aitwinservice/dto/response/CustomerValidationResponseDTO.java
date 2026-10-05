package org.example.aitwinservice.dto.response;

public record CustomerValidationResponseDTO(

        Long customerId,
        Long userId,
        String status,
        boolean valid

) {
}