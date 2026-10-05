package org.example.customerservice.dto.response;

import org.example.customerservice.enums.CustomerStatus;

public record CustomerClaimSummaryResponseDTO(

        Long customerId,
        String fullName,
        String email,
        String phone,
        CustomerStatus status

) {
}