package org.example.aiactionservice.dto.request;

// Carries common action data and action-specific simulation facts.
// The backend validates that only fields relevant to the selected
// action type are populated before rule evaluation starts.

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.aiactionservice.enums.ActionType;
import org.example.aiactionservice.enums.BookingOutcome;
import org.example.aiactionservice.enums.PurchaseOutcome;
import org.example.aiactionservice.enums.SubscriptionOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SimulateActionRequestDTO(

        @NotNull(message = "AI Twin ID is required")
        Long twinId,

        @NotNull(message = "Action type is required")
        ActionType actionType,

        @NotBlank(message = "Transaction reference is required")
        @Size(
                min = 3,
                max = 100,
                message = "Transaction reference must contain 3 to 100 characters"
        )
        String transactionReference,

        @NotBlank(message = "Action description is required")
        @Size(
                min = 5,
                max = 500,
                message = "Description must contain 5 to 500 characters"
        )
        String description,

        @DecimalMin(
                value = "0.01",
                message = "Action amount must be greater than zero"
        )
        BigDecimal actionAmount,

        @NotNull(message = "Approval information is required")
        Boolean approvalProvided,

        BookingOutcome bookingOutcome,

        PurchaseOutcome purchaseOutcome,

        SubscriptionOperation subscriptionOperation,

        Boolean cancellationCompleted,

        LocalDateTime occurredAt

) {
}