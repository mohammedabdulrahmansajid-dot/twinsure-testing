package org.example.aiactionservice.dto.response;

// Represents an evaluated action in Customer and Admin lists.
// Scenario fields explain the simulated result without exposing
// the database model directly.

import org.example.aiactionservice.enums.ActionStatus;
import org.example.aiactionservice.enums.ActionType;
import org.example.aiactionservice.enums.BookingOutcome;
import org.example.aiactionservice.enums.PurchaseOutcome;
import org.example.aiactionservice.enums.SubscriptionOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AiActionResponseDTO(

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
        LocalDateTime occurredAt,
        LocalDateTime evaluatedAt,
        LocalDateTime createdAt

) {
}