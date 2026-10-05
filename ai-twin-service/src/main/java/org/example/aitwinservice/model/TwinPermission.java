package org.example.aitwinservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aitwinservice.enums.ActionType;
import org.example.aitwinservice.enums.PermissionLevel;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("TWIN_PERMISSIONS")
public class TwinPermission {

    @Id
    @Column("PERMISSION_ID")
    private Long permissionId;

    @Column("TWIN_ID")
    private Long twinId;

    @Column("ACTION_TYPE")
    private ActionType actionType;

    @Column("PERMISSION_LEVEL")
    private PermissionLevel permissionLevel;

    @Column("ACTION_LIMIT")
    private BigDecimal actionLimit;

    @Column("ACTIVE")
    private Boolean active;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;
}