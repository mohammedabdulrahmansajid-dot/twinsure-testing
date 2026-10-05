package org.example.customerservice.dto.response;

import org.example.customerservice.enums.CustomerStatus;

public record CustomerValidationResponseDTO(

        Long customerId,
        Long userId,
        CustomerStatus status,
        boolean valid

) {
}