package org.example.insurancepolicyservice.model;

// Stores one underwritten AI Twin action permission at policy issuance.
// Permission level and action limit remain available for later audit.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("POLICY_PERMISSION_SNAPSHOTS")
public class PolicyPermissionSnapshot {

    @Id
    @Column("PERMISSION_SNAPSHOT_ID")
    private Long permissionSnapshotId;

    @Column("POLICY_ID")
    private Long policyId;

    @Column("ACTION_TYPE")
    private String actionType;

    @Column("PERMISSION_LEVEL")
    private String permissionLevel;

    @Column("ACTION_LIMIT")
    private BigDecimal actionLimit;

    @Column("ACTIVE")
    private Boolean active;
}