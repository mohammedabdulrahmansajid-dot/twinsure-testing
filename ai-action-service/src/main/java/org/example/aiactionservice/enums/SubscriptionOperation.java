package org.example.aiactionservice.enums;

// Describes the Subscription Management operation being simulated.
// A cancellation that is not completed produces MISSED_CANCELLATION.

public enum SubscriptionOperation {

    CREATE_OR_RENEW,
    CANCEL
}