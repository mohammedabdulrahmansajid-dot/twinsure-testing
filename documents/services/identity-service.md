# Identity Service Documentation

1. **Purpose:** Handles authentication, user registration, role assignment, password encoding, and session verification.
2. **Main Responsibilities:** Issues HttpOnly JWT cookies (`JWT-TOKEN`), validates active/suspended account statuses, registers customers and staff accounts.
3. **Technology and Persistence:** Java 17, Spring Boot 3.4.3, Spring WebFlux, Spring Security, R2DBC + H2 (`identitydb`).
4. **Important Packages/Components:** `org.example.identityservice.service.UserService`, `org.example.identityservice.controller.AuthController`, `org.example.identityservice.utility.JwtUtil`.
5. **Main Domain Entities:** `User` (userId, username, password, role, customerId, status, createdAt, updatedAt).
6. **Main DTO Categories:** `RegisterRequestDTO`, `LoginRequestDTO`, `UserRegistrationResponseDTO`, `AuthResponseDTO`.
7. **Main API Groups:** `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`, `/api/auth/me`.
8. **Authentication and Authorization:** Issues signed JWT tokens written directly to HttpOnly cookie. Public registration defaults to `CUSTOMER` role. Staff account endpoints require `ADMIN` role.
9. **Cross-Service Dependencies:** Invokes `customer-service` upon customer registration to create profile.
10. **Important Validations:** Jakarta `@NotBlank`, `@Size(min = 6)` for passwords, duplicate username checks.
11. **Important Exceptions:** `UsernameAlreadyExistsException`, `UserNotFoundException`, `InvalidCredentialsException`, `InvalidStaffRoleException`.
12. **Logging/Monitoring/Actuator Support:** SLF4J logging for login attempts and security events.
13. **Database Ownership:** Isolated `identitydb` table `users`.
14. **Existing Tests:** `IdentityServiceApplicationTests.java`.
15. **Tests Added:** `UserServiceTest.java` verifying customer registration and duplicate username prevention.
16. **Testing Limitations:** Reactive StepVerifier unit tests present; full integration test requires active database context.
17. **Known Limitations:** In-memory H2 database resets on service restart.
18. **Key Configuration Requirements:** `server.port=8081`, `spring.r2dbc.url=r2dbc:h2:mem:///identitydb`.
