package org.example.aiactionservice.model;

// Stores one rule violation detected for an evaluated AI action.
// A separate model is required because a single action may violate
// multiple rules such as approval missing and limit exceeded.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aiactionservice.enums.ViolationSeverity;
import org.example.aiactionservice.enums.ViolationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("ACTION_VIOLATIONS")
public class ActionViolation {

    @Id
    @Column("VIOLATION_ID")
    private Long violationId;

    @Column("ACTION_ID")
    private Long actionId;

    @Column("VIOLATION_TYPE")
    private ViolationType violationType;

    @Column("SEVERITY")
    private ViolationSeverity severity;

    @Column("DESCRIPTION")
    private String description;

    @Column("CLAIM_ELIGIBLE")
    private Boolean claimEligible;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;
}