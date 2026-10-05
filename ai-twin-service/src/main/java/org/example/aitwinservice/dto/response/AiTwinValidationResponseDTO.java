package org.example.aitwinservice.dto.response;

import org.example.aitwinservice.enums.AiTwinStatus;

public record AiTwinValidationResponseDTO(

        Long twinId,
        Long customerId,
        AiTwinStatus status,
        boolean ownedByCustomer,
        boolean valid

) {
}