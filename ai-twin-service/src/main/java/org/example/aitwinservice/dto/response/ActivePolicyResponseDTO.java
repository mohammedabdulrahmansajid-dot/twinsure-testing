package org.example.aitwinservice.dto.response;

// Represents the active-policy status returned by Insurance Policy Service.
// AI Twin Service uses this contract to protect underwritten configuration
// from being changed during an active insurance policy term.

import java.time.LocalDate;

public record ActivePolicyResponseDTO(

        Long policyId,
        Long customerId,
        Long twinId,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        boolean active

) {
}