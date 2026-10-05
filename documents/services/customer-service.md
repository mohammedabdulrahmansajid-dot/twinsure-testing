# Customer Service Documentation

1. **Purpose:** Manages customer personal details, address information, profile updates, and customer account statuses.
2. **Main Responsibilities:** Customer profile creation, duplicate profile prevention, owner profile retrieval, Admin customer lookup and status management.
3. **Technology and Persistence:** Java 17, Spring Boot 3.4.3, Spring WebFlux, Spring Data R2DBC, H2 (`customerdb`).
4. **Important Packages/Components:** `org.example.customerservice.service.CustomerService`, `org.example.customerservice.controller.CustomerController`, `org.example.customerservice.repo.CustomerRepo`.
5. **Main Domain Entities:** `Customer` (customerId, userId, fullName, email, phone, address, status, createdAt, updatedAt).
6. **Main DTO Categories:** `CustomerCreateRequestDTO`, `CustomerUpdateRequestDTO`, `CustomerResponseDTO`.
7. **Main API Groups:** `/api/customers/me`, `/api/customers/profile`, `/api/customers/admin/list`.
8. **Authentication and Authorization:** Secured via JWT forwarded from API Gateway. Customer users access `/me`; Administrators access `/admin/*`.
9. **Cross-Service Dependencies:** Called by `identity-service` during customer signup. Linked logically by `customerId` across `ai-twin-service`, `insurance-policy-service`, and `claims-service`.
10. **Important Validations:** Email pattern checking, non-empty full name, unique `userId` and `email` constraints.
11. **Important Exceptions:** `CustomerNotFoundException`, `DuplicateCustomerException`, `UnauthorizedAccessException`.
12. **Logging/Monitoring/Actuator Support:** SLF4J logging for customer profile creation and update operations.
13. **Database Ownership:** Isolated `customerdb` table `customers`.
14. **Existing Tests:** `CustomerServiceApplicationTests.java`.
15. **Tests Added:** None executed due to missing `spring-boot-starter-test` dependency in `pom.xml`.
16. **Testing Limitations:** Requires adding `spring-boot-starter-test` and `reactor-test` to `pom.xml` for full unit testing.
17. **Known Limitations:** Reactive R2DBC repository lacks full JPA cascading.
18. **Key Configuration Requirements:** `server.port=8082`, `spring.r2dbc.url=r2dbc:h2:mem:///customerdb`.
