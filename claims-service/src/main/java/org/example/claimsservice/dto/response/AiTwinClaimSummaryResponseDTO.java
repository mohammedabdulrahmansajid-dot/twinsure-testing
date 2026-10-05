package org.example.claimsservice.dto.response;

// Represents claim-related AI Twin information received from AI Twin Service.
// Claims staff use it to compare the reported action with the Twin's
// configured autonomy, limits, status, and permissions.

import java.math.BigDecimal;
import java.util.List;

public record AiTwinClaimSummaryResponseDTO(

        Long twinId,
        Long customerId,
        String twinName,
        String providerName,
        String modelName,
        String autonomyLevel,
        BigDecimal transactionLimit,
        BigDecimal approvalThreshold,
        String status,
        List<AiTwinPermissionSummaryResponseDTO> permissions

) {
}