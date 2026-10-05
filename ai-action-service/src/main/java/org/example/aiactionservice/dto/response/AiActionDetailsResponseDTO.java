package org.example.aiactionservice.dto.response;

// Combines the evaluated action, scenario facts, and all violations.
// The response gives Customers and claim reviewers a complete
// explanation of how the action was evaluated.

import org.example.aiactionservice.enums.ActionStatus;
import org.example.aiactionservice.enums.ActionType;
import org.example.aiactionservice.enums.BookingOutcome;
import org.example.aiactionservice.enums.PurchaseOutcome;
import org.example.aiactionservice.enums.SubscriptionOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AiActionDetailsResponseDTO(

        Long actionId,
        Long customerId,
        Long twinId,
        Long policyId,
        ActionType actionType,
        String transactionReference,
        String description,
        BigDecimal actionAmount,
        Boolean approvalProvided,
        BookingOutcome bookingOutcome,
        PurchaseOutcome purchaseOutcome,
        SubscriptionOperation subscriptionOperation,
        Boolean cancellationCompleted,
        ActionStatus actionStatus,
        Boolean insured,
        List<ActionViolationResponseDTO> violations,
        LocalDateTime occurredAt,
        LocalDateTime evaluatedAt,
        LocalDateTime createdAt

) {
}