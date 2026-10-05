package org.example.claimsservice.dto.response;

// Combines the formal claim, incident, documents, and decision history.
// It provides one complete view for the Customer, Claims Adjuster, or Admin.

import java.util.List;

public record ClaimDetailsResponseDTO(

        ClaimResponseDTO claim,
        IncidentResponseDTO incident,
        List<ClaimDocumentResponseDTO> documents,
        List<ClaimDecisionResponseDTO> decisions

) {
}