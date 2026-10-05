package org.example.customerservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.customerservice.enums.CustomerStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("CUSTOMERS")
public class Customer {

    @Id
    @Column("CUSTOMER_ID")
    private Long customerId;

    @Column("USER_ID")
    private Long userId;

    @Column("FULL_NAME")
    private String fullName;

    @Column("EMAIL")
    private String email;

    @Column("PHONE")
    private String phone;

    @Column("ADDRESS")
    private String address;

    @Column("STATUS")
    private CustomerStatus status;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;
}