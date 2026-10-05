package org.example.insurancepolicyservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.insurancepolicyservice.enums.PolicyApplicationStatus;
import org.example.insurancepolicyservice.enums.RiskLevel;
import org.example.insurancepolicyservice.enums.UnderwritingRecommendation;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Stores a Customer's request to insure an AI Twin and tracks
 * the complete underwriting workflow. It holds the calculated
 * risk, proposed premium, Underwriter decision, and acceptance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("POLICY_APPLICATIONS")
public class PolicyApplication {

    @Id
    @Column("APPLICATION_ID")
    private Long applicationId;

    @Column("CUSTOMER_ID")
    private Long customerId;

    @Column("TWIN_ID")
    private Long twinId;

    @Column("PRODUCT_ID")
    private Long productId;

    @Column("STATUS")
    private PolicyApplicationStatus status;

    @Column("RISK_SCORE")
    private Integer riskScore;

    @Column("RISK_LEVEL")
    private RiskLevel riskLevel;

    @Column("SYSTEM_RECOMMENDATION")
    private UnderwritingRecommendation systemRecommendation;

    @Column("PROPOSED_PREMIUM")
    private BigDecimal proposedPremium;

    @Column("PROPOSED_COVERAGE_LIMIT")
    private BigDecimal proposedCoverageLimit;

    @Column("PROPOSED_DEDUCTIBLE")
    private BigDecimal proposedDeductible;

    @Column("REVIEWED_BY")
    private Long reviewedBy;

    @Column("DECISION_REASON")
    private String decisionReason;

    @Column("SUBMITTED_AT")
    private LocalDateTime submittedAt;

    @Column("REVIEWED_AT")
    private LocalDateTime reviewedAt;

    @Column("PROPOSAL_EXPIRES_AT")
    private LocalDateTime proposalExpiresAt;

    @Column("ACCEPTED_AT")
    private LocalDateTime acceptedAt;
}