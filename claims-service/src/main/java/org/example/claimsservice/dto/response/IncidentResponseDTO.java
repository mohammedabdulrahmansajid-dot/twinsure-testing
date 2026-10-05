package org.example.claimsservice.dto.response;

// Represents a reported incident in Customer and staff API responses.
// It contains the linked Customer, Twin, policy, action, financial loss,
// incident status, and important timestamps.

import org.example.claimsservice.enums.IncidentStatus;
import org.example.claimsservice.enums.IncidentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IncidentResponseDTO(

        Long incidentId,
        Long customerId,
        Long twinId,
        Long policyId,
        Long actionId,
        IncidentType incidentType,
        String description,
        BigDecimal lossAmount,
        LocalDateTime actionOccurredAt,
        LocalDateTime reportedAt,
        IncidentStatus status

) {
}