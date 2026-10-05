package org.example.claimsservice.dto.response;

// Represents claim-related Customer information received from Customer Service.
// Claims staff use this summary while reviewing a Customer's formal claim.

public record CustomerClaimSummaryResponseDTO(

        Long customerId,
        String fullName,
        String email,
        String phone,
        String status

) {
}