package org.example.insurancepolicyservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/*
 * Stores situations that an insurance product does not cover.
 * These exclusions help Claims Service reject events such as
 * intentional actions or actions outside the policy period.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("PRODUCT_EXCLUSIONS")
public class ProductExclusion {

    @Id
    @Column("EXCLUSION_ID")
    private Long exclusionId;

    @Column("PRODUCT_ID")
    private Long productId;

    @Column("EXCLUSION_CODE")
    private String exclusionCode;

    @Column("DESCRIPTION")
    private String description;

    @Column("ACTIVE")
    private Boolean active;
}