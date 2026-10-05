package org.example.claimsservice.dto.response;

// Represents one active policy exclusion received from Insurance Policy Service.
// Claims Service uses exclusions to identify situations that are not insured.

public record PolicyExclusionResponseDTO(

        Long exclusionId,
        Long productId,
        String exclusionCode,
        String description,
        Boolean active

) {
}