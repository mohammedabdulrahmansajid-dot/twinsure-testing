package org.example.claimsservice.dto.response;

// Returns the updated claim together with the newly stored decision.
// It allows the caller to see both the current claim state
// and the audit record created for the workflow action.

public record ClaimDecisionResultResponseDTO(

        ClaimResponseDTO claim,
        ClaimDecisionResponseDTO decision

) {
}