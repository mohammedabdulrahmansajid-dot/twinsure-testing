package org.example.aitwinservice.dto.response;

// Explains whether Customer configuration changes are currently allowed.
// The response exposes only policy-lock information needed by the UI.

import java.time.LocalDate;

public record AiTwinConfigurationLockResponseDTO(

        Long twinId,
        boolean locked,
        Long policyId,
        String policyStatus,
        LocalDate policyStartDate,
        LocalDate policyEndDate,
        String explanation

) {
}