package org.example.aiactionservice.model;

// Stores an evaluated AI Twin action and its simulation scenario.
// Scenario fields preserve the facts that caused violations so action
// history, incident reporting, and claim investigation remain auditable.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aiactionservice.enums.ActionStatus;
import org.example.aiactionservice.enums.ActionType;
import org.example.aiactionservice.enums.BookingOutcome;
import org.example.aiactionservice.enums.PurchaseOutcome;
import org.example.aiactionservice.enums.SubscriptionOperation;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("AI_ACTIONS")
public class AiAction {

    @Id
    @Column("ACTION_ID")
    private Long actionId;

    @Column("CUSTOMER_ID")
    private Long customerId;

    @Column("TWIN_ID")
    private Long twinId;

    @Column("POLICY_ID")
    private Long policyId;

    @Column("ACTION_TYPE")
    private ActionType actionType;

    @Column("TRANSACTION_REFERENCE")
    private String transactionReference;

    @Column("DESCRIPTION")
    private String description;

    @Column("ACTION_AMOUNT")
    private BigDecimal actionAmount;

    @Column("APPROVAL_PROVIDED")
    private Boolean approvalProvided;

    @Column("BOOKING_OUTCOME")
    private BookingOutcome bookingOutcome;

    @Column("PURCHASE_OUTCOME")
    private PurchaseOutcome purchaseOutcome;

    @Column("SUBSCRIPTION_OPERATION")
    private SubscriptionOperation subscriptionOperation;

    @Column("CANCELLATION_COMPLETED")
    private Boolean cancellationCompleted;

    @Column("ACTION_STATUS")
    private ActionStatus actionStatus;

    @Column("INSURED")
    private Boolean insured;

    @Column("OCCURRED_AT")
    private LocalDateTime occurredAt;

    @Column("EVALUATED_AT")
    private LocalDateTime evaluatedAt;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;
}