package org.example.customerservice.dto.response;

import org.example.customerservice.enums.CustomerStatus;

import java.time.LocalDateTime;

public record CustomerProfileResponseDTO(

        Long customerId,
        Long userId,
        String fullName,
        String email,
        String phone,
        String address,
        CustomerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}