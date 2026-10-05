package org.example.aiactionservice.service;

// Evaluates action permissions, scenario outcomes, financial limits,
// and approval requirements. Every applicable violation is collected,
// so one violation never suppresses another valid violation.

import org.example.aiactionservice.dto.request.SimulateActionRequestDTO;
import org.example.aiactionservice.dto.response.AiTwinActionRulesResponseDTO;
import org.example.aiactionservice.dto.response.AiTwinPermissionResponseDTO;
import org.example.aiactionservice.enums.ActionType;
import org.example.aiactionservice.enums.BookingOutcome;
import org.example.aiactionservice.enums.PurchaseOutcome;
import org.example.aiactionservice.enums.SubscriptionOperation;
import org.example.aiactionservice.enums.ViolationSeverity;
import org.example.aiactionservice.enums.ViolationType;
import org.example.aiactionservice.exception.AiTwinValidationException;
import org.example.aiactionservice.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class ActionRuleEvaluator {

    public List<ViolationResult> evaluate(
            SimulateActionRequestDTO request,
            AiTwinActionRulesResponseDTO twinRules,
            boolean insured) {

        validateActionRules(
                request,
                twinRules
        );

        validateScenarioFields(
                request
        );

        AiTwinPermissionResponseDTO permission =
                findPermission(
                        request,
                        twinRules
                );

        List<ViolationResult> violations =
                new ArrayList<>();

        Set<ViolationType> detectedTypes =
                EnumSet.noneOf(
                        ViolationType.class
                );

        evaluateScenarioOutcome(
                request,
                insured,
                detectedTypes,
                violations
        );

        evaluateProhibitedAction(
                permission,
                insured,
                detectedTypes,
                violations
        );

        evaluateFinancialLimits(
                request,
                twinRules,
                permission,
                insured,
                detectedTypes,
                violations
        );

        evaluateApprovalRequirement(
                request,
                twinRules,
                permission,
                insured,
                detectedTypes,
                violations
        );

        return List.copyOf(
                violations
        );
    }

    private void validateActionRules(
            SimulateActionRequestDTO request,
            AiTwinActionRulesResponseDTO twinRules) {

        if (request == null) {

            throw new InvalidRequestException(
                    "Action simulation request is required"
            );
        }

        if (request.actionType() == null) {

            throw new InvalidRequestException(
                    "Action type is required"
            );
        }

        if (twinRules == null) {

            throw new AiTwinValidationException(
                    "AI Twin action rules are unavailable"
            );
        }

        if (!twinRules.valid()) {

            throw new AiTwinValidationException(
                    "AI Twin is inactive or does not belong "
                            + "to the authenticated Customer"
            );
        }

        if (!"ACTIVE".equals(
                twinRules.status()
        )) {

            throw new AiTwinValidationException(
                    "Only an ACTIVE AI Twin can simulate actions"
            );
        }

        if (twinRules.transactionLimit() == null
                || twinRules.approvalThreshold()
                == null) {

            throw new AiTwinValidationException(
                    "AI Twin financial rules are incomplete"
            );
        }

        if (twinRules.permissions() == null) {

            throw new AiTwinValidationException(
                    "AI Twin permissions are unavailable"
            );
        }

        validateActionAmount(
                request
        );
    }

    private void validateActionAmount(
            SimulateActionRequestDTO request) {

        boolean amountRequired =
                request.actionType()
                        == ActionType.TRAVEL_BOOKING
                        || request.actionType()
                        == ActionType.ONLINE_PURCHASE;

        if (amountRequired
                && request.actionAmount() == null) {

            throw new InvalidRequestException(
                    "Action amount is required for "
                            + request.actionType()
            );
        }

        if (request.actionAmount() != null
                && request.actionAmount()
                .compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new InvalidRequestException(
                    "Action amount must be greater than zero"
            );
        }
    }

    private void validateScenarioFields(
            SimulateActionRequestDTO request) {

        switch (request.actionType()) {

            case TRAVEL_BOOKING ->
                    validateTravelScenario(
                            request
                    );

            case ONLINE_PURCHASE ->
                    validatePurchaseScenario(
                            request
                    );

            case SUBSCRIPTION_MANAGEMENT ->
                    validateSubscriptionScenario(
                            request
                    );
        }
    }

    private void validateTravelScenario(
            SimulateActionRequestDTO request) {

        if (request.bookingOutcome() == null) {

            throw new InvalidRequestException(
                    "Booking outcome is required "
                            + "for Travel Booking"
            );
        }

        if (request.purchaseOutcome() != null
                || request.subscriptionOperation()
                != null
                || request.cancellationCompleted()
                != null) {

            throw new InvalidRequestException(
                    "Travel Booking must not contain "
                            + "purchase or subscription scenario fields"
            );
        }
    }

    private void validatePurchaseScenario(
            SimulateActionRequestDTO request) {

        if (request.purchaseOutcome() == null) {

            throw new InvalidRequestException(
                    "Purchase outcome is required "
                            + "for Online Purchase"
            );
        }

        if (request.bookingOutcome() != null
                || request.subscriptionOperation()
                != null
                || request.cancellationCompleted()
                != null) {

            throw new InvalidRequestException(
                    "Online Purchase must not contain "
                            + "booking or subscription scenario fields"
            );
        }
    }

    private void validateSubscriptionScenario(
            SimulateActionRequestDTO request) {

        if (request.subscriptionOperation()
                == null) {

            throw new InvalidRequestException(
                    "Subscription operation is required "
                            + "for Subscription Management"
            );
        }

        if (request.bookingOutcome() != null
                || request.purchaseOutcome() != null) {

            throw new InvalidRequestException(
                    "Subscription Management must not contain "
                            + "booking or purchase outcome fields"
            );
        }

        if (request.subscriptionOperation()
                == SubscriptionOperation.CANCEL
                && request.cancellationCompleted()
                == null) {

            throw new InvalidRequestException(
                    "Cancellation completion information "
                            + "is required for a cancellation"
            );
        }

        if (request.subscriptionOperation()
                == SubscriptionOperation
                .CREATE_OR_RENEW
                && request.cancellationCompleted()
                != null) {

            throw new InvalidRequestException(
                    "Cancellation completion must be empty "
                            + "for Create or Renew"
            );
        }
    }

    private AiTwinPermissionResponseDTO findPermission(
            SimulateActionRequestDTO request,
            AiTwinActionRulesResponseDTO twinRules) {

        return twinRules.permissions()
                .stream()
                .filter(permission ->
                        Boolean.TRUE.equals(
                                permission.active()
                        )
                )
                .filter(permission ->
                        request.actionType()
                                .name()
                                .equals(
                                        permission.actionType()
                                )
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException(
                                "No active permission is configured for "
                                        + request.actionType()
                        )
                );
    }

    private void evaluateScenarioOutcome(
            SimulateActionRequestDTO request,
            boolean insured,
            Set<ViolationType> detectedTypes,
            List<ViolationResult> violations) {

        if (request.actionType()
                == ActionType.TRAVEL_BOOKING
                && request.bookingOutcome()
                == BookingOutcome.WRONG_BOOKING) {

            addViolation(
                    ViolationType.WRONG_ACTION,
                    ViolationSeverity.HIGH,
                    "The AI Twin completed a Travel Booking "
                            + "with an incorrect booking outcome",
                    insured,
                    detectedTypes,
                    violations
            );
        }

        if (request.actionType()
                == ActionType.ONLINE_PURCHASE
                && request.purchaseOutcome()
                == PurchaseOutcome.WRONG_PURCHASE) {

            addViolation(
                    ViolationType.WRONG_ACTION,
                    ViolationSeverity.HIGH,
                    "The AI Twin completed an incorrect "
                            + "Online Purchase",
                    insured,
                    detectedTypes,
                    violations
            );
        }

        if (request.actionType()
                == ActionType.ONLINE_PURCHASE
                && request.purchaseOutcome()
                == PurchaseOutcome.DUPLICATE_PURCHASE) {

            addViolation(
                    ViolationType.DUPLICATE_ACTION,
                    ViolationSeverity.HIGH,
                    "The AI Twin created a duplicate "
                            + "Online Purchase",
                    insured,
                    detectedTypes,
                    violations
            );
        }

        if (request.actionType()
                == ActionType.SUBSCRIPTION_MANAGEMENT
                && request.subscriptionOperation()
                == SubscriptionOperation.CANCEL
                && !Boolean.TRUE.equals(
                request.cancellationCompleted()
        )) {

            addViolation(
                    ViolationType.MISSED_CANCELLATION,
                    ViolationSeverity.HIGH,
                    "The AI Twin was requested to cancel "
                            + "a subscription, but the cancellation "
                            + "was not completed",
                    insured,
                    detectedTypes,
                    violations
            );
        }
    }

    private void evaluateProhibitedAction(
            AiTwinPermissionResponseDTO permission,
            boolean insured,
            Set<ViolationType> detectedTypes,
            List<ViolationResult> violations) {

        if ("PROHIBITED".equals(
                permission.permissionLevel()
        )) {

            addViolation(
                    ViolationType.PROHIBITED_ACTION,
                    ViolationSeverity.CRITICAL,
                    "The AI Twin attempted an action "
                            + "that is explicitly prohibited",
                    insured,
                    detectedTypes,
                    violations
            );
        }
    }

    private void evaluateFinancialLimits(
            SimulateActionRequestDTO request,
            AiTwinActionRulesResponseDTO twinRules,
            AiTwinPermissionResponseDTO permission,
            boolean insured,
            Set<ViolationType> detectedTypes,
            List<ViolationResult> violations) {

        BigDecimal actionAmount =
                request.actionAmount();

        if (actionAmount == null) {
            return;
        }

        BigDecimal effectiveLimit =
                determineEffectiveLimit(
                        twinRules.transactionLimit(),
                        permission.actionLimit()
                );

        if (effectiveLimit == null) {
            return;
        }

        if (actionAmount.compareTo(
                effectiveLimit
        ) > 0) {

            addViolation(
                    ViolationType.LIMIT_EXCEEDED,
                    ViolationSeverity.HIGH,
                    "Action amount "
                            + actionAmount
                            + " exceeded the effective limit "
                            + effectiveLimit,
                    insured,
                    detectedTypes,
                    violations
            );
        }
    }

    private BigDecimal determineEffectiveLimit(
            BigDecimal overallLimit,
            BigDecimal actionLimit) {

        if (overallLimit == null) {
            return actionLimit;
        }

        if (actionLimit == null) {
            return overallLimit;
        }

        return overallLimit.min(
                actionLimit
        );
    }

    private void evaluateApprovalRequirement(
            SimulateActionRequestDTO request,
            AiTwinActionRulesResponseDTO twinRules,
            AiTwinPermissionResponseDTO permission,
            boolean insured,
            Set<ViolationType> detectedTypes,
            List<ViolationResult> violations) {

        boolean permissionNeedsApproval =
                "APPROVAL_REQUIRED".equals(
                        permission.permissionLevel()
                );

        boolean thresholdNeedsApproval =
                request.actionAmount() != null
                        && request.actionAmount()
                        .compareTo(
                                twinRules.approvalThreshold()
                        ) > 0;

        boolean approvalMissing =
                (permissionNeedsApproval
                        || thresholdNeedsApproval)
                        && !Boolean.TRUE.equals(
                        request.approvalProvided()
                );

        if (approvalMissing) {

            addViolation(
                    ViolationType.APPROVAL_MISSING,
                    ViolationSeverity.HIGH,
                    "The action required Customer approval, "
                            + "but approval was not provided",
                    insured,
                    detectedTypes,
                    violations
            );
        }
    }

    private void addViolation(
            ViolationType violationType,
            ViolationSeverity severity,
            String description,
            boolean claimEligible,
            Set<ViolationType> detectedTypes,
            List<ViolationResult> violations) {

        if (!detectedTypes.add(
                violationType
        )) {
            return;
        }

        violations.add(
                new ViolationResult(
                        violationType,
                        severity,
                        description,
                        claimEligible
                )
        );
    }

    public record ViolationResult(

            ViolationType violationType,
            ViolationSeverity severity,
            String description,
            boolean claimEligible

    ) {
    }
}