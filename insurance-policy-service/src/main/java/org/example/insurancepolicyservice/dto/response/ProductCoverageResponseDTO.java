package org.example.insurancepolicyservice.dto.response;

import org.example.insurancepolicyservice.enums.ActionType;
import org.example.insurancepolicyservice.enums.ViolationType;

import java.math.BigDecimal;

public record ProductCoverageResponseDTO(

        Long coverageId,
        Long productId,
        ActionType actionType,
        ViolationType violationType,
        BigDecimal coverageLimit,
        Boolean active,
        String description

) {
}