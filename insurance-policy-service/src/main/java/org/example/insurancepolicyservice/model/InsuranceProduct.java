package org.example.insurancepolicyservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.insurancepolicyservice.enums.ProductStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Stores the main insurance plan created and managed by an Admin.
 * It provides the base premium, maximum coverage, and deductible
 * later used during underwriting and claim assessment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("INSURANCE_PRODUCTS")
public class InsuranceProduct {

    @Id
    @Column("PRODUCT_ID")
    private Long productId;

    @Column("PRODUCT_CODE")
    private String productCode;

    @Column("PRODUCT_NAME")
    private String productName;

    @Column("DESCRIPTION")
    private String description;

    @Column("BASE_PREMIUM")
    private BigDecimal basePremium;

    @Column("COVERAGE_LIMIT")
    private BigDecimal coverageLimit;

    @Column("DEDUCTIBLE")
    private BigDecimal deductible;

    @Column("STATUS")
    private ProductStatus status;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;
}
