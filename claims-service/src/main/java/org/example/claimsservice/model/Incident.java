package org.example.claimsservice.model;

// Stores a Customer-reported incident linked to an AI action and policy.
// The incident captures the loss and action date before a formal claim
// is created and reviewed by a Claims Adjuster.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.claimsservice.enums.IncidentStatus;
import org.example.claimsservice.enums.IncidentType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("INCIDENTS")
public class Incident {

    @Id
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

    @Column("INCIDENT_TYPE")
    private IncidentType incidentType;

    @Column("DESCRIPTION")
    private String description;

    @Column("LOSS_AMOUNT")
    private BigDecimal lossAmount;

    @Column("ACTION_OCCURRED_AT")
    private LocalDateTime actionOccurredAt;

    @Column("REPORTED_AT")
    private LocalDateTime reportedAt;

    @Column("STATUS")
    private IncidentStatus status;
}