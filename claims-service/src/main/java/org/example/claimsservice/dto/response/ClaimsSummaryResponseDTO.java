package org.example.claimsservice.dto.response;

// Provides simple claim counts for an Admin or Claims Adjuster dashboard.
// It summarizes pending, active-review, completed, and total claims.

public record ClaimsSummaryResponseDTO(

        long submitted,
        long assigned,
        long underReview,
        long moreInformationRequired,
        long approved,
        long partiallyApproved,
        long rejected,
        long closed,
        long total

) {
}