package org.example.claimsservice.dto.response;

// Represents one AI Twin permission included in the claim-summary response.
// It helps the Claims Adjuster understand the action-specific rule
// that applied when the incident occurred.

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AiTwinPermissionSummaryResponseDTO(

        Long permissionId,
        Long twinId,
        String actionType,
        String permissionLevel,
        BigDecimal actionLimit,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}