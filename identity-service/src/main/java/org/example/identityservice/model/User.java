package org.example.identityservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.identityservice.enums.Role;
import org.example.identityservice.enums.UserStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("USERS")
public class User {

    @Id
    @Column("USER_ID")
    private Long userId;

    @Column("USERNAME")
    private String username;

    @Column("PASSWORD")
    private String password;

    @Column("ROLE")
    private Role role;

    @Column("CUSTOMER_ID")
    private Long customerId;

    @Column("STATUS")
    private UserStatus status;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;
}