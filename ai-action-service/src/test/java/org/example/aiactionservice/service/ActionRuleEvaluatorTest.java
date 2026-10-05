package org.example.aiactionservice.service;

// Tests the central AI action evaluation rules without starting Spring.
// It covers compliant actions, prohibited actions, financial limits,
// approval requirements, insurance eligibility, and invalid Twin rules.

import org.example.aiactionservice.dto.request.SimulateActionRequestDTO;
import org.example.aiactionservice.dto.response.AiTwinActionRulesResponseDTO;
import org.example.aiactionservice.dto.response.AiTwinPermissionResponseDTO;
import org.example.aiactionservice.enums.ActionType;
import org.example.aiactionservice.enums.ViolationSeverity;
import org.example.aiactionservice.enums.ViolationType;
import org.example.aiactionservice.exception.AiTwinValidationException;
import org.example.aiactionservice.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionRuleEvaluatorTest {

    private ActionRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {

        evaluator = new ActionRuleEvaluator();
    }

    @Test
    void shouldReturnNoViolationsForCompliantAction() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.ONLINE_PURCHASE,
                        "3000",
                        true
                );

        AiTwinActionRulesResponseDTO rules =
                createRules(
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        ActionType.ONLINE_PURCHASE,
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        List<ActionRuleEvaluator.ViolationResult> results =
                evaluator.evaluate(
                        request,
                        rules,
                        true
                );

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldDetectProhibitedAction() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.SUBSCRIPTION_MANAGEMENT,
                        null,
                        false
                );

        AiTwinActionRulesResponseDTO rules =
                createRules(
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        ActionType.SUBSCRIPTION_MANAGEMENT,
                                        "PROHIBITED",
                                        null
                                )
                        )
                );

        List<ActionRuleEvaluator.ViolationResult> results =
                evaluator.evaluate(
                        request,
                        rules,
                        true
                );

        assertEquals(
                1,
                results.size()
        );

        assertEquals(
                ViolationType.PROHIBITED_ACTION,
                results.get(0).violationType()
        );

        assertEquals(
                ViolationSeverity.CRITICAL,
                results.get(0).severity()
        );

        assertTrue(
                results.get(0).claimEligible()
        );
    }

    @Test
    void shouldDetectActionSpecificLimitExceeded() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.ONLINE_PURCHASE,
                        "7000",
                        true
                );

        AiTwinActionRulesResponseDTO rules =
                createRules(
                        "10000",
                        "9000",
                        List.of(
                                createPermission(
                                        ActionType.ONLINE_PURCHASE,
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        List<ActionRuleEvaluator.ViolationResult> results =
                evaluator.evaluate(
                        request,
                        rules,
                        true
                );

        assertEquals(
                1,
                results.size()
        );

        assertEquals(
                ViolationType.LIMIT_EXCEEDED,
                results.get(0).violationType()
        );

        assertEquals(
                ViolationSeverity.HIGH,
                results.get(0).severity()
        );
    }

    @Test
    void shouldDetectOverallTransactionLimitExceeded() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.TRAVEL_BOOKING,
                        "12000",
                        true
                );

        AiTwinActionRulesResponseDTO rules =
                createRules(
                        "10000",
                        "15000",
                        List.of(
                                createPermission(
                                        ActionType.TRAVEL_BOOKING,
                                        "ALLOWED",
                                        "20000"
                                )
                        )
                );

        List<ActionRuleEvaluator.ViolationResult> results =
                evaluator.evaluate(
                        request,
                        rules,
                        true
                );

        assertEquals(
                1,
                results.size()
        );

        assertEquals(
                ViolationType.LIMIT_EXCEEDED,
                results.get(0).violationType()
        );
    }

    @Test
    void shouldDetectMissingApprovalFromPermissionLevel() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.ONLINE_PURCHASE,
                        "3000",
                        false
                );

        AiTwinActionRulesResponseDTO rules =
                createRules(
                        "10000",
                        "8000",
                        List.of(
                                createPermission(
                                        ActionType.ONLINE_PURCHASE,
                                        "APPROVAL_REQUIRED",
                                        "5000"
                                )
                        )
                );

        List<ActionRuleEvaluator.ViolationResult> results =
                evaluator.evaluate(
                        request,
                        rules,
                        true
                );

        assertEquals(
                1,
                results.size()
        );

        assertEquals(
                ViolationType.APPROVAL_MISSING,
                results.get(0).violationType()
        );
    }

    @Test
    void shouldDetectMissingApprovalAboveThreshold() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.TRAVEL_BOOKING,
                        "7000",
                        false
                );

        AiTwinActionRulesResponseDTO rules =
                createRules(
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        ActionType.TRAVEL_BOOKING,
                                        "ALLOWED",
                                        "9000"
                                )
                        )
                );

        List<ActionRuleEvaluator.ViolationResult> results =
                evaluator.evaluate(
                        request,
                        rules,
                        true
                );

        assertEquals(
                1,
                results.size()
        );

        assertEquals(
                ViolationType.APPROVAL_MISSING,
                results.get(0).violationType()
        );
    }

    @Test
    void shouldDetectMultipleViolationsForOneAction() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.TRAVEL_BOOKING,
                        "18000",
                        false
                );

        AiTwinActionRulesResponseDTO rules =
                createRules(
                        "25000",
                        "15000",
                        List.of(
                                createPermission(
                                        ActionType.TRAVEL_BOOKING,
                                        "ALLOWED",
                                        "10000"
                                )
                        )
                );

        List<ActionRuleEvaluator.ViolationResult> results =
                evaluator.evaluate(
                        request,
                        rules,
                        true
                );

        assertEquals(
                2,
                results.size()
        );

        assertTrue(
                containsViolation(
                        results,
                        ViolationType.LIMIT_EXCEEDED
                )
        );

        assertTrue(
                containsViolation(
                        results,
                        ViolationType.APPROVAL_MISSING
                )
        );
    }

    @Test
    void shouldMarkViolationAsNotClaimEligibleWithoutInsurance() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.ONLINE_PURCHASE,
                        "7000",
                        true
                );

        AiTwinActionRulesResponseDTO rules =
                createRules(
                        "10000",
                        "9000",
                        List.of(
                                createPermission(
                                        ActionType.ONLINE_PURCHASE,
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        List<ActionRuleEvaluator.ViolationResult> results =
                evaluator.evaluate(
                        request,
                        rules,
                        false
                );

        assertEquals(
                1,
                results.size()
        );

        assertEquals(
                ViolationType.LIMIT_EXCEEDED,
                results.get(0).violationType()
        );

        assertFalse(
                results.get(0).claimEligible()
        );
    }

    @Test
    void shouldRejectFinancialActionWithoutAmount() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.ONLINE_PURCHASE,
                        null,
                        true
                );

        AiTwinActionRulesResponseDTO rules =
                createRules(
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        ActionType.ONLINE_PURCHASE,
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> evaluator.evaluate(
                                request,
                                rules,
                                true
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Action amount is required"
                        )
        );
    }

    @Test
    void shouldRejectInactiveAiTwin() {

        SimulateActionRequestDTO request =
                createRequest(
                        ActionType.ONLINE_PURCHASE,
                        "3000",
                        true
                );

        AiTwinActionRulesResponseDTO rules =
                new AiTwinActionRulesResponseDTO(
                        1L,
                        1L,
                        "INACTIVE",
                        "SUPERVISED",
                        new BigDecimal("10000"),
                        new BigDecimal("5000"),
                        List.of(
                                createPermission(
                                        ActionType.ONLINE_PURCHASE,
                                        "ALLOWED",
                                        "5000"
                                )
                        ),
                        true
                );

        AiTwinValidationException exception =
                assertThrows(
                        AiTwinValidationException.class,
                        () -> evaluator.evaluate(
                                request,
                                rules,
                                true
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "ACTIVE AI Twin"
                        )
        );
    }

    private SimulateActionRequestDTO createRequest(
            ActionType actionType,
            String actionAmount,
            boolean approvalProvided) {

        BigDecimal amount =
                actionAmount == null
                        ? null
                        : new BigDecimal(
                        actionAmount
                );

        return new SimulateActionRequestDTO(
                1L,
                actionType,
                "UNIT-TEST-REFERENCE",
                "Unit test action description",
                amount,
                approvalProvided,
                LocalDateTime.now()
        );
    }

    private AiTwinActionRulesResponseDTO createRules(
            String transactionLimit,
            String approvalThreshold,
            List<AiTwinPermissionResponseDTO> permissions) {

        return new AiTwinActionRulesResponseDTO(
                1L,
                1L,
                "ACTIVE",
                "SUPERVISED",
                new BigDecimal(
                        transactionLimit
                ),
                new BigDecimal(
                        approvalThreshold
                ),
                permissions,
                true
        );
    }

    private AiTwinPermissionResponseDTO createPermission(
            ActionType actionType,
            String permissionLevel,
            String actionLimit) {

        BigDecimal limit =
                actionLimit == null
                        ? null
                        : new BigDecimal(
                        actionLimit
                );

        return new AiTwinPermissionResponseDTO(
                1L,
                1L,
                actionType.name(),
                permissionLevel,
                limit,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private boolean containsViolation(
            List<ActionRuleEvaluator.ViolationResult> results,
            ViolationType violationType) {

        return results.stream()
                .anyMatch(result ->
                        result.violationType()
                                == violationType
                );
    }
}