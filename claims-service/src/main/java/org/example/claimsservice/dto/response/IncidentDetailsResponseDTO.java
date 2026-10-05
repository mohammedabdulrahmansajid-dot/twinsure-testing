package org.example.claimsservice.dto.response;

// Combines the stored incident with the validated AI action evidence.
// This gives Customers and claim staff a complete view of why the
// incident was reported and which AI rule was violated.

public record IncidentDetailsResponseDTO(

        IncidentResponseDTO incident,
        AiActionEvidenceResponseDTO actionEvidence

) {
}