package org.example.insurancepolicyservice.model;

// Stores the AI Twin configuration captured when a policy is issued.
// The immutable snapshot preserves the risk controls used for underwriting.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("POLICY_TWIN_SNAPSHOTS")
public class PolicyTwinSnapshot {

    @Id
    @Column("SNAPSHOT_ID")
    private Long snapshotId;

    @Column("POLICY_ID")
    private Long policyId;

    @Column("TWIN_ID")
    private Long twinId;

    @Column("TWIN_NAME")
    private String twinName;

    @Column("AUTONOMY_LEVEL")
    private String autonomyLevel;

    @Column("TRANSACTION_LIMIT")
    private BigDecimal transactionLimit;

    @Column("APPROVAL_THRESHOLD")
    private BigDecimal approvalThreshold;

    @Column("TWIN_STATUS")
    private String twinStatus;

    @Column("CAPTURED_AT")
    private LocalDateTime capturedAt;
}