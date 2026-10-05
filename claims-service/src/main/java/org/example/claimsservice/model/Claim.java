package org.example.claimsservice.model;

// Stores the formal insurance claim created from a validated incident.
// This model tracks claimed and approved amounts, claim status,
// assigned Adjuster, submission time, and closure time.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.claimsservice.enums.ClaimStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("CLAIMS")
public class Claim {

    @Id
    @Column("CLAIM_ID")
    private Long claimId;

    @Column("CLAIM_NUMBER")
    private String claimNumber;

    @Column("INCIDENT_ID")
    private Long incidentId;

    @Column("CUSTOMER_ID")
    private Long customerId;

    @Column("TWIN_ID")
    private Long twinId;

    @Column("POLICY_ID")
    private Long policyId;

    @Column("ACTION_ID")
    private Long actionId;

    @Column("CLAIMED_AMOUNT")
    private BigDecimal claimedAmount;

    @Column("APPROVED_AMOUNT")
    private BigDecimal approvedAmount;

    @Column("DEDUCTIBLE_APPLIED")
    private BigDecimal deductibleApplied;

    @Column("STATUS")
    private ClaimStatus status;

    @Column("ASSIGNED_ADJUSTER_ID")
    private Long assignedAdjusterId;

    @Column("SUBMITTED_AT")
    private LocalDateTime submittedAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column("CLOSED_AT")
    private LocalDateTime closedAt;
}