package org.example.customerservice.dto.response;

import org.example.customerservice.enums.CustomerStatus;

public record CustomerResponseDTO(

        Long customerId,
        Long userId,
        String fullName,
        String email,
        String phone,
        CustomerStatus status

) {
}