package org.example.aitwinservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aitwinservice.enums.AiTwinStatus;
import org.example.aitwinservice.enums.AutonomyLevel;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("AI_TWINS")
public class AiTwin {

    @Id
    @Column("TWIN_ID")
    private Long twinId;

    @Column("CUSTOMER_ID")
    private Long customerId;

    @Column("TWIN_NAME")
    private String twinName;

    @Column("PROVIDER_NAME")
    private String providerName;

    @Column("MODEL_NAME")
    private String modelName;

    @Column("AUTONOMY_LEVEL")
    private AutonomyLevel autonomyLevel;

    @Column("TRANSACTION_LIMIT")
    private BigDecimal transactionLimit;

    @Column("APPROVAL_THRESHOLD")
    private BigDecimal approvalThreshold;

    @Column("STATUS")
    private AiTwinStatus status;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;
}