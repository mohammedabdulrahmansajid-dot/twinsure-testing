package org.example.aiactionservice.dto.response;

// Returns the immediate result of simulating and evaluating an AI action.
// It summarizes compliance, insurance status, and all detected violations
// after the action and violation records have been saved.

import org.example.aiactionservice.enums.ActionStatus;
import org.example.aiactionservice.enums.ActionType;

import java.util.List;

public record ActionEvaluationResponseDTO(

        Long actionId,
        Long customerId,
        Long twinId,
        Long policyId,
        ActionType actionType,
        String transactionReference,
        ActionStatus actionStatus,
        boolean insured,
        boolean compliant,
        boolean claimEligible,
        List<ActionViolationResponseDTO> violations,
        String message

) {
}