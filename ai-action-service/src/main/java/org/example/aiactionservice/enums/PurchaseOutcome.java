package org.example.aiactionservice.enums;

// Describes the simulated result of an Online Purchase action.
// Wrong and duplicate outcomes produce separate rule violations.

public enum PurchaseOutcome {

    COMPLETED_CORRECTLY,
    WRONG_PURCHASE,
    DUPLICATE_PURCHASE
}