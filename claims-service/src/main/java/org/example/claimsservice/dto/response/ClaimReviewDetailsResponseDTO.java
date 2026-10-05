package org.example.claimsservice.dto.response;

// Provides the complete information needed by a Claims Adjuster.
// It combines the stored claim with Customer, AI Twin, AI action,
// policy coverage, documents, and previous decision history.

import java.util.List;

public record ClaimReviewDetailsResponseDTO(

        ClaimResponseDTO claim,
        IncidentResponseDTO incident,
        CustomerClaimSummaryResponseDTO customer,
        AiTwinClaimSummaryResponseDTO aiTwin,
        AiActionEvidenceResponseDTO actionEvidence,
        PolicyCoverageResponseDTO policyCoverage,
        List<ClaimDocumentResponseDTO> documents,
        List<ClaimDecisionResponseDTO> decisions

) {
}