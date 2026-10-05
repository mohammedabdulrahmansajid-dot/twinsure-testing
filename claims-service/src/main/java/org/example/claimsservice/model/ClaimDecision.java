package org.example.claimsservice.model;

// Stores an audit record for every Claims Adjuster workflow decision.
// It preserves the previous status, new status, approved amount,
// reason, Adjuster ID, and decision timestamp.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.claimsservice.enums.ClaimDecisionType;
import org.example.claimsservice.enums.ClaimStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("CLAIM_DECISIONS")
public class ClaimDecision {

    @Id
    @Column("DECISION_ID")
    private Long decisionId;

    @Column("CLAIM_ID")
    private Long claimId;

    @Column("ADJUSTER_ID")
    private Long adjusterId;

    @Column("DECISION_TYPE")
    private ClaimDecisionType decisionType;

    @Column("PREVIOUS_STATUS")
    private ClaimStatus previousStatus;

    @Column("NEW_STATUS")
    private ClaimStatus newStatus;

    @Column("APPROVED_AMOUNT")
    private BigDecimal approvedAmount;

    @Column("REASON")
    private String reason;

    @Column("DECIDED_AT")
    private LocalDateTime decidedAt;
}