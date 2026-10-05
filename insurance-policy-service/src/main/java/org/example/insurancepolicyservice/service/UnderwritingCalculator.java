package org.example.insurancepolicyservice.service;

// Calculates an AI Twin's underwriting risk score and proposed premium.
// The system provides a recommendation, while the Underwriter makes
// the final approval, change-request, or rejection decision.

import org.example.insurancepolicyservice.dto.request.RiskAssessmentRequestDTO;
import org.example.insurancepolicyservice.dto.response.AiTwinPermissionResponseDTO;
import org.example.insurancepolicyservice.dto.response.AiTwinRiskProfileResponseDTO;
import org.example.insurancepolicyservice.dto.response.RiskAssessmentResponseDTO;
import org.example.insurancepolicyservice.enums.RiskLevel;
import org.example.insurancepolicyservice.enums.UnderwritingRecommendation;
import org.example.insurancepolicyservice.exception.InvalidRequestException;
import org.example.insurancepolicyservice.model.InsuranceProduct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class UnderwritingCalculator {

    public RiskAssessmentResponseDTO calculateRisk(
            Long applicationId,
            InsuranceProduct product,
            AiTwinRiskProfileResponseDTO twinProfile,
            RiskAssessmentRequestDTO request) {

        validateRiskProfile(twinProfile);

        int autonomyPoints =
                calculateAutonomyPoints(
                        twinProfile.autonomyLevel()
                );

        int transactionLimitPoints =
                calculateTransactionLimitPoints(
                        twinProfile.transactionLimit()
                );

        int approvalControlPoints =
                calculateApprovalControlPoints(
                        twinProfile.transactionLimit(),
                        twinProfile.approvalThreshold()
                );

        int permissionPoints =
                calculatePermissionPoints(
                        twinProfile.permissions()
                );

        int prohibitedControlAdjustment =
                calculateProhibitedControlAdjustment(
                        twinProfile.permissions()
                );

        int incidentHistoryPoints =
                calculateIncidentHistoryPoints(
                        request.previousIncidentCount()
                );

        int rawRiskScore =
                autonomyPoints
                        + transactionLimitPoints
                        + approvalControlPoints
                        + permissionPoints
                        + prohibitedControlAdjustment
                        + incidentHistoryPoints;

        int finalRiskScore =
                Math.max(
                        0,
                        Math.min(
                                100,
                                rawRiskScore
                        )
                );

        RiskLevel riskLevel =
                determineRiskLevel(finalRiskScore);

        BigDecimal riskMultiplier =
                determineRiskMultiplier(riskLevel);

        BigDecimal calculatedPremium =
                product.getBasePremium()
                        .multiply(riskMultiplier)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        UnderwritingRecommendation recommendation =
                determineRecommendation(riskLevel);

        String explanation =
                buildExplanation(
                        autonomyPoints,
                        transactionLimitPoints,
                        approvalControlPoints,
                        permissionPoints,
                        prohibitedControlAdjustment,
                        incidentHistoryPoints,
                        finalRiskScore,
                        riskLevel
                );

        return new RiskAssessmentResponseDTO(
                applicationId,
                finalRiskScore,
                riskLevel,
                riskMultiplier,
                product.getBasePremium(),
                calculatedPremium,
                product.getCoverageLimit(),
                product.getDeductible(),
                recommendation,
                explanation
        );
    }

    private int calculateAutonomyPoints(
            String autonomyLevel) {

        return switch (autonomyLevel) {

            case "ASSISTIVE" -> 10;

            case "SUPERVISED" -> 25;

            case "AUTONOMOUS" -> 45;

            default ->
                    throw new InvalidRequestException(
                            "Unsupported autonomy level: "
                                    + autonomyLevel
                    );
        };
    }

    private int calculateTransactionLimitPoints(
            BigDecimal transactionLimit) {

        BigDecimal tenThousand =
                new BigDecimal("10000");

        BigDecimal twentyFiveThousand =
                new BigDecimal("25000");

        if (transactionLimit.compareTo(
                tenThousand
        ) <= 0) {

            return 5;
        }

        if (transactionLimit.compareTo(
                twentyFiveThousand
        ) <= 0) {

            return 15;
        }

        return 25;
    }

    private int calculateApprovalControlPoints(
            BigDecimal transactionLimit,
            BigDecimal approvalThreshold) {

        /*
         * A threshold below the transaction limit means
         * some transactions require Customer approval.
         * This control reduces underwriting risk.
         */
        if (approvalThreshold.compareTo(
                transactionLimit
        ) < 0) {

            return -10;
        }

        /*
         * If both values are equal, nearly every transaction
         * within the limit can happen without extra approval.
         * This increases underwriting risk.
         */
        return 20;
    }

    private int calculatePermissionPoints(
            List<AiTwinPermissionResponseDTO> permissions) {

        long permittedActionCount =
                permissions.stream()
                        .filter(permission ->
                                Boolean.TRUE.equals(
                                        permission.active()
                                )
                        )
                        .filter(permission ->
                                !"PROHIBITED".equals(
                                        permission.permissionLevel()
                                )
                        )
                        .count();

        if (permittedActionCount <= 1) {
            return 5;
        }

        if (permittedActionCount == 2) {
            return 10;
        }

        return 20;
    }

    private int calculateProhibitedControlAdjustment(
            List<AiTwinPermissionResponseDTO> permissions) {

        boolean hasProhibitedAction =
                permissions.stream()
                        .filter(permission ->
                                Boolean.TRUE.equals(
                                        permission.active()
                                )
                        )
                        .anyMatch(permission ->
                                "PROHIBITED".equals(
                                        permission.permissionLevel()
                                )
                        );

        if (hasProhibitedAction) {
            return -5;
        }

        return 0;
    }

    private int calculateIncidentHistoryPoints(
            int previousIncidentCount) {

        if (previousIncidentCount == 0) {
            return 0;
        }

        if (previousIncidentCount == 1) {
            return 10;
        }

        return 20;
    }

    private RiskLevel determineRiskLevel(
            int riskScore) {

        if (riskScore <= 25) {
            return RiskLevel.LOW;
        }

        if (riskScore <= 50) {
            return RiskLevel.MEDIUM;
        }

        if (riskScore <= 75) {
            return RiskLevel.HIGH;
        }

        return RiskLevel.VERY_HIGH;
    }

    private BigDecimal determineRiskMultiplier(
            RiskLevel riskLevel) {

        return switch (riskLevel) {

            case LOW ->
                    new BigDecimal("1.00");

            case MEDIUM ->
                    new BigDecimal("1.25");

            case HIGH ->
                    new BigDecimal("1.50");

            case VERY_HIGH ->
                    new BigDecimal("2.00");
        };
    }

    private UnderwritingRecommendation determineRecommendation(
            RiskLevel riskLevel) {

        return switch (riskLevel) {

            case LOW, MEDIUM ->
                    UnderwritingRecommendation.APPROVE;

            case HIGH ->
                    UnderwritingRecommendation.MANUAL_REVIEW;

            case VERY_HIGH ->
                    UnderwritingRecommendation.REQUEST_CHANGES;
        };
    }

    private void validateRiskProfile(
            AiTwinRiskProfileResponseDTO twinProfile) {

        if (twinProfile == null) {
            throw new InvalidRequestException(
                    "AI Twin risk profile is required"
            );
        }

        if (!"ACTIVE".equals(twinProfile.status())) {
            throw new InvalidRequestException(
                    "Risk assessment can only be performed "
                            + "for an ACTIVE AI Twin"
            );
        }

        if (twinProfile.transactionLimit() == null
                || twinProfile.approvalThreshold() == null) {

            throw new InvalidRequestException(
                    "AI Twin financial limits are incomplete"
            );
        }

        if (twinProfile.permissions() == null
                || twinProfile.permissions().isEmpty()) {

            throw new InvalidRequestException(
                    "AI Twin must have at least one "
                            + "configured permission"
            );
        }
    }

    private String buildExplanation(
            int autonomyPoints,
            int transactionLimitPoints,
            int approvalControlPoints,
            int permissionPoints,
            int prohibitedControlAdjustment,
            int incidentHistoryPoints,
            int finalRiskScore,
            RiskLevel riskLevel) {

        return "Autonomy points: "
                + autonomyPoints
                + ", transaction-limit points: "
                + transactionLimitPoints
                + ", approval-control points: "
                + approvalControlPoints
                + ", permission points: "
                + permissionPoints
                + ", prohibited-control adjustment: "
                + prohibitedControlAdjustment
                + ", incident-history points: "
                + incidentHistoryPoints
                + ". Final risk score: "
                + finalRiskScore
                + ", risk level: "
                + riskLevel
                + ".";
    }
}