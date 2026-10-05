package org.example.insurancepolicyservice.model;

// Stores the final policy issued after a Customer accepts an approved proposal.
// The policy contains coverage dates and financial terms used during claims.
// It also records the reason when an Admin cancels the policy.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.insurancepolicyservice.enums.PolicyStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("POLICIES")
public class Policy {

    @Id
    @Column("POLICY_ID")
    private Long policyId;

    @Column("POLICY_NUMBER")
    private String policyNumber;

    @Column("APPLICATION_ID")
    private Long applicationId;

    @Column("CUSTOMER_ID")
    private Long customerId;

    @Column("TWIN_ID")
    private Long twinId;

    @Column("PRODUCT_ID")
    private Long productId;

    @Column("PREMIUM")
    private BigDecimal premium;

    @Column("COVERAGE_LIMIT")
    private BigDecimal coverageLimit;

    @Column("DEDUCTIBLE")
    private BigDecimal deductible;

    @Column("START_DATE")
    private LocalDate startDate;

    @Column("END_DATE")
    private LocalDate endDate;

    @Column("STATUS")
    private PolicyStatus status;

    @Column("CANCELLATION_REASON")
    private String cancellationReason;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;
}