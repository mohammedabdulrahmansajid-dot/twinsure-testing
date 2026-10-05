package org.example.insurancepolicyservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.insurancepolicyservice.enums.ActionType;
import org.example.insurancepolicyservice.enums.ViolationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/*
 * Defines which AI action and violation combinations are covered
 * by an insurance product. Claims Service later uses this data
 * to determine whether a reported AI mistake is insured.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("PRODUCT_COVERAGES")
public class ProductCoverage {

    @Id
    @Column("COVERAGE_ID")
    private Long coverageId;

    @Column("PRODUCT_ID")
    private Long productId;

    @Column("ACTION_TYPE")
    private ActionType actionType;

    @Column("VIOLATION_TYPE")
    private ViolationType violationType;

    @Column("COVERAGE_LIMIT")
    private BigDecimal coverageLimit;

    @Column("ACTIVE")
    private Boolean active;

    @Column("DESCRIPTION")
    private String description;
}