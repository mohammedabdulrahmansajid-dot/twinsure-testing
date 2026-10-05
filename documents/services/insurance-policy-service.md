# Insurance Policy Service Documentation

1. **Purpose:** Manages insurance product offerings, policy applications, underwriting risk calculations, decisions, and issued policy lifecycles.
2. **Main Responsibilities:** Product catalog management, policy application creation, risk scoring (`UnderwritingCalculator`), approval/rejection/change-request workflows, policy issuance, coverage lookup.
3. **Technology and Persistence:** Java 17, Spring Boot 3.4.3, Spring WebFlux, R2DBC + H2 (`policydb`), WebClient load balancer.
4. **Important Packages/Components:** `org.example.insurancepolicyservice.service.PolicyApplicationService`, `org.example.insurancepolicyservice.service.UnderwritingCalculator`, `org.example.insurancepolicyservice.controller.PolicyApplicationController`.
5. **Main Domain Entities:** `InsuranceProduct`, `PolicyApplication`, `Policy`, `Coverage`, `Exclusion`.
6. **Main DTO Categories:** `PolicyApplicationRequestDTO`, `UnderwritingDecisionRequestDTO`, `PolicyResponseDTO`, `RiskAssessmentResponseDTO`.
7. **Main API Groups:** `/api/products`, `/api/policy-applications`, `/api/policies`.
8. **Authentication and Authorization:** Secured via Gateway. Customers submit applications; Underwriters review and record decisions; Admins create products.
9. **Cross-Service Dependencies:** Calls `ai-twin-service` to fetch twin risk profiles and lock configurations upon policy issuance.
10. **Important Validations:** Coverage limit minimums, risk score capping (0–100), product active status checks, proposal expiration.
11. **Important Exceptions:** `ProductNotFoundException`, `PolicyApplicationNotFoundException`, `InvalidUnderwritingDecisionException`.
12. **Logging/Monitoring/Actuator Support:** SLF4J logging; Spring Boot Actuator enabled (`/actuator/health`).
13. **Database Ownership:** Isolated `policydb` tables `insurance_products`, `policy_applications`, `policies`, `coverages`, `exclusions`.
14. **Existing Tests:** `InsurancePolicyServiceApplicationTests.java`, `UnderwritingCalculatorTest.java`.
15. **Tests Added:** Executed 12 passing unit tests in `UnderwritingCalculatorTest`.
16. **Testing Limitations:** WebClient cross-service calls require WireMock for integration test suites.
17. **Known Limitations:** In-memory H2 DB; automated proposal expiration requires scheduler trigger.
18. **Key Configuration Requirements:** `server.port=8084`, `spring.r2dbc.url=r2dbc:h2:mem:///policydb`.
