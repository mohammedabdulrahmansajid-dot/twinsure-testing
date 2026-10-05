package org.example.insurancepolicyservice.service;

// Tests underwriting risk scoring and premium calculations without starting Spring.
// It covers all risk levels, score capping, control adjustments,
// incident history, and invalid AI Twin risk-profile configurations.

import org.example.insurancepolicyservice.dto.request.RiskAssessmentRequestDTO;
import org.example.insurancepolicyservice.dto.response.AiTwinPermissionResponseDTO;
import org.example.insurancepolicyservice.dto.response.AiTwinRiskProfileResponseDTO;
import org.example.insurancepolicyservice.dto.response.RiskAssessmentResponseDTO;
import org.example.insurancepolicyservice.enums.ProductStatus;
import org.example.insurancepolicyservice.enums.RiskLevel;
import org.example.insurancepolicyservice.enums.UnderwritingRecommendation;
import org.example.insurancepolicyservice.exception.InvalidRequestException;
import org.example.insurancepolicyservice.model.InsuranceProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnderwritingCalculatorTest {

    private UnderwritingCalculator calculator;
    private InsuranceProduct product;

    @BeforeEach
    void setUp() {

        calculator = new UnderwritingCalculator();
        product = createInsuranceProduct();
    }

    @Test
    void shouldCalculateLowRiskAndBasePremium() {

        AiTwinRiskProfileResponseDTO profile =
                createRiskProfile(
                        "ASSISTIVE",
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                ),
                                createPermission(
                                        2L,
                                        "SUBSCRIPTION_MANAGEMENT",
                                        "PROHIBITED",
                                        null
                                )
                        )
                );

        RiskAssessmentRequestDTO request =
                createAssessmentRequest(0);

        RiskAssessmentResponseDTO response =
                calculator.calculateRisk(
                        1L,
                        product,
                        profile,
                        request
                );

        assertEquals(
                5,
                response.riskScore()
        );

        assertEquals(
                RiskLevel.LOW,
                response.riskLevel()
        );

        assertBigDecimalEquals(
                "1.00",
                response.riskMultiplier()
        );

        assertBigDecimalEquals(
                "3000.00",
                response.calculatedPremium()
        );

        assertEquals(
                UnderwritingRecommendation.APPROVE,
                response.recommendation()
        );
    }

    @Test
    void shouldCalculateMediumRiskAndPremiumMultiplier() {

        AiTwinRiskProfileResponseDTO profile =
                createRiskProfile(
                        "SUPERVISED",
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        RiskAssessmentRequestDTO request =
                createAssessmentRequest(1);

        RiskAssessmentResponseDTO response =
                calculator.calculateRisk(
                        2L,
                        product,
                        profile,
                        request
                );

        assertEquals(
                35,
                response.riskScore()
        );

        assertEquals(
                RiskLevel.MEDIUM,
                response.riskLevel()
        );

        assertBigDecimalEquals(
                "1.25",
                response.riskMultiplier()
        );

        assertBigDecimalEquals(
                "3750.00",
                response.calculatedPremium()
        );

        assertEquals(
                UnderwritingRecommendation.APPROVE,
                response.recommendation()
        );
    }

    @Test
    void shouldCalculateHighRiskAndManualReviewRecommendation() {

        AiTwinRiskProfileResponseDTO profile =
                createRiskProfile(
                        "AUTONOMOUS",
                        "20000",
                        "10000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                ),
                                createPermission(
                                        2L,
                                        "TRAVEL_BOOKING",
                                        "ALLOWED",
                                        "10000"
                                )
                        )
                );

        RiskAssessmentRequestDTO request =
                createAssessmentRequest(0);

        RiskAssessmentResponseDTO response =
                calculator.calculateRisk(
                        3L,
                        product,
                        profile,
                        request
                );

        assertEquals(
                60,
                response.riskScore()
        );

        assertEquals(
                RiskLevel.HIGH,
                response.riskLevel()
        );

        assertBigDecimalEquals(
                "1.50",
                response.riskMultiplier()
        );

        assertBigDecimalEquals(
                "4500.00",
                response.calculatedPremium()
        );

        assertEquals(
                UnderwritingRecommendation.MANUAL_REVIEW,
                response.recommendation()
        );
    }

    @Test
    void shouldCalculateVeryHighRiskAndRequestChanges() {

        AiTwinRiskProfileResponseDTO profile =
                createRiskProfile(
                        "AUTONOMOUS",
                        "30000",
                        "30000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                ),
                                createPermission(
                                        2L,
                                        "TRAVEL_BOOKING",
                                        "ALLOWED",
                                        "10000"
                                ),
                                createPermission(
                                        3L,
                                        "SUBSCRIPTION_MANAGEMENT",
                                        "ALLOWED",
                                        "2000"
                                )
                        )
                );

        RiskAssessmentRequestDTO request =
                createAssessmentRequest(2);

        RiskAssessmentResponseDTO response =
                calculator.calculateRisk(
                        4L,
                        product,
                        profile,
                        request
                );

        assertEquals(
                100,
                response.riskScore()
        );

        assertEquals(
                RiskLevel.VERY_HIGH,
                response.riskLevel()
        );

        assertBigDecimalEquals(
                "2.00",
                response.riskMultiplier()
        );

        assertBigDecimalEquals(
                "6000.00",
                response.calculatedPremium()
        );

        assertEquals(
                UnderwritingRecommendation.REQUEST_CHANGES,
                response.recommendation()
        );
    }

    @Test
    void shouldCapRiskScoreAtOneHundred() {

        AiTwinRiskProfileResponseDTO profile =
                createRiskProfile(
                        "AUTONOMOUS",
                        "50000",
                        "50000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "20000"
                                ),
                                createPermission(
                                        2L,
                                        "TRAVEL_BOOKING",
                                        "ALLOWED",
                                        "20000"
                                ),
                                createPermission(
                                        3L,
                                        "SUBSCRIPTION_MANAGEMENT",
                                        "ALLOWED",
                                        "10000"
                                )
                        )
                );

        RiskAssessmentRequestDTO request =
                createAssessmentRequest(10);

        RiskAssessmentResponseDTO response =
                calculator.calculateRisk(
                        5L,
                        product,
                        profile,
                        request
                );

        assertEquals(
                100,
                response.riskScore()
        );

        assertEquals(
                RiskLevel.VERY_HIGH,
                response.riskLevel()
        );
    }

    @Test
    void shouldReduceRiskScoreWhenProhibitedControlExists() {

        AiTwinRiskProfileResponseDTO profileWithoutProhibition =
                createRiskProfile(
                        "SUPERVISED",
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        AiTwinRiskProfileResponseDTO profileWithProhibition =
                createRiskProfile(
                        "SUPERVISED",
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                ),
                                createPermission(
                                        2L,
                                        "SUBSCRIPTION_MANAGEMENT",
                                        "PROHIBITED",
                                        null
                                )
                        )
                );

        RiskAssessmentRequestDTO request =
                createAssessmentRequest(0);

        RiskAssessmentResponseDTO responseWithoutProhibition =
                calculator.calculateRisk(
                        6L,
                        product,
                        profileWithoutProhibition,
                        request
                );

        RiskAssessmentResponseDTO responseWithProhibition =
                calculator.calculateRisk(
                        7L,
                        product,
                        profileWithProhibition,
                        request
                );

        assertEquals(
                5,
                responseWithoutProhibition.riskScore()
                        - responseWithProhibition.riskScore()
        );
    }

    @Test
    void shouldIncreaseRiskScoreForPreviousIncidents() {

        AiTwinRiskProfileResponseDTO profile =
                createRiskProfile(
                        "SUPERVISED",
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        RiskAssessmentResponseDTO noIncidentResponse =
                calculator.calculateRisk(
                        8L,
                        product,
                        profile,
                        createAssessmentRequest(0)
                );

        RiskAssessmentResponseDTO oneIncidentResponse =
                calculator.calculateRisk(
                        9L,
                        product,
                        profile,
                        createAssessmentRequest(1)
                );

        RiskAssessmentResponseDTO multipleIncidentResponse =
                calculator.calculateRisk(
                        10L,
                        product,
                        profile,
                        createAssessmentRequest(2)
                );

        assertEquals(
                10,
                oneIncidentResponse.riskScore()
                        - noIncidentResponse.riskScore()
        );

        assertEquals(
                20,
                multipleIncidentResponse.riskScore()
                        - noIncidentResponse.riskScore()
        );
    }

    @Test
    void shouldReturnProductCoverageAndDeductible() {

        AiTwinRiskProfileResponseDTO profile =
                createRiskProfile(
                        "ASSISTIVE",
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        RiskAssessmentResponseDTO response =
                calculator.calculateRisk(
                        11L,
                        product,
                        profile,
                        createAssessmentRequest(0)
                );

        assertBigDecimalEquals(
                "50000.00",
                response.recommendedCoverageLimit()
        );

        assertBigDecimalEquals(
                "2000.00",
                response.recommendedDeductible()
        );

        assertBigDecimalEquals(
                "3000.00",
                response.basePremium()
        );
    }

    @Test
    void shouldRejectInactiveAiTwin() {

        AiTwinRiskProfileResponseDTO profile =
                new AiTwinRiskProfileResponseDTO(
                        1L,
                        1L,
                        "Nova",
                        "SUPERVISED",
                        new BigDecimal("10000"),
                        new BigDecimal("5000"),
                        "INACTIVE",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> calculator.calculateRisk(
                                12L,
                                product,
                                profile,
                                createAssessmentRequest(0)
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "ACTIVE AI Twin"
                        )
        );
    }

    @Test
    void shouldRejectMissingPermissions() {

        AiTwinRiskProfileResponseDTO profile =
                createRiskProfile(
                        "SUPERVISED",
                        "10000",
                        "5000",
                        List.of()
                );

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> calculator.calculateRisk(
                                13L,
                                product,
                                profile,
                                createAssessmentRequest(0)
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "at least one configured permission"
                        )
        );
    }

    @Test
    void shouldRejectUnsupportedAutonomyLevel() {

        AiTwinRiskProfileResponseDTO profile =
                createRiskProfile(
                        "UNKNOWN_AUTONOMY",
                        "10000",
                        "5000",
                        List.of(
                                createPermission(
                                        1L,
                                        "ONLINE_PURCHASE",
                                        "ALLOWED",
                                        "5000"
                                )
                        )
                );

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> calculator.calculateRisk(
                                14L,
                                product,
                                profile,
                                createAssessmentRequest(0)
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Unsupported autonomy level"
                        )
        );
    }

    private InsuranceProduct createInsuranceProduct() {

        InsuranceProduct insuranceProduct =
                new InsuranceProduct();

        insuranceProduct.setProductId(
                1L
        );

        insuranceProduct.setProductCode(
                "TSP-AI-001"
        );

        insuranceProduct.setProductName(
                "TwinSure AI Protection"
        );

        insuranceProduct.setDescription(
                "Insurance coverage for AI Twin actions"
        );

        insuranceProduct.setBasePremium(
                new BigDecimal("3000.00")
        );

        insuranceProduct.setCoverageLimit(
                new BigDecimal("50000.00")
        );

        insuranceProduct.setDeductible(
                new BigDecimal("2000.00")
        );

        insuranceProduct.setStatus(
                ProductStatus.ACTIVE
        );

        insuranceProduct.setCreatedAt(
                LocalDateTime.now()
        );

        insuranceProduct.setUpdatedAt(
                LocalDateTime.now()
        );

        return insuranceProduct;
    }

    private RiskAssessmentRequestDTO createAssessmentRequest(
            int previousIncidentCount) {

        return new RiskAssessmentRequestDTO(
                previousIncidentCount,
                "Risk-assessment unit test"
        );
    }

    private AiTwinRiskProfileResponseDTO createRiskProfile(
            String autonomyLevel,
            String transactionLimit,
            String approvalThreshold,
            List<AiTwinPermissionResponseDTO> permissions) {

        return new AiTwinRiskProfileResponseDTO(
                1L,
                1L,
                "Nova",
                autonomyLevel,
                new BigDecimal(
                        transactionLimit
                ),
                new BigDecimal(
                        approvalThreshold
                ),
                "ACTIVE",
                permissions
        );
    }

    private AiTwinPermissionResponseDTO createPermission(
            Long permissionId,
            String actionType,
            String permissionLevel,
            String actionLimit) {

        BigDecimal limit =
                actionLimit == null
                        ? null
                        : new BigDecimal(
                        actionLimit
                );

        return new AiTwinPermissionResponseDTO(
                permissionId,
                1L,
                actionType,
                permissionLevel,
                limit,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private void assertBigDecimalEquals(
            String expected,
            BigDecimal actual) {

        assertEquals(
                0,
                new BigDecimal(expected)
                        .compareTo(actual)
        );
    }
}