package org.example.insurancepolicyservice.dto.response;

// Represents one AI Twin action permission captured at policy issuance.
// The contract is historical and does not change with current Twin settings.

import java.math.BigDecimal;

public record PolicyPermissionSnapshotResponseDTO(

        String actionType,
        String permissionLevel,
        BigDecimal actionLimit,
        Boolean active

) {
}