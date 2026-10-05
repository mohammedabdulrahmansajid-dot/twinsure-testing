package org.example.claimsservice.dto.response;

// Represents one product coverage rule received from Insurance Policy Service.
// Claims Service matches the AI action type and violation type against
// this rule to determine whether the reported incident is covered.

import java.math.BigDecimal;

public record PolicyCoverageRuleResponseDTO(

        Long coverageId,
        Long productId,
        String actionType,
        String violationType,
        BigDecimal coverageLimit,
        Boolean active,
        String description

) {
}