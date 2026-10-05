package org.example.insurancepolicyservice.dto.response;

public record ProductExclusionResponseDTO(

        Long exclusionId,
        Long productId,
        String exclusionCode,
        String description,
        Boolean active

) {
}