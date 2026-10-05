# AI Twin Service Documentation

1. **Purpose:** Manages AI Twin registration, financial control thresholds, action permissions, and risk profiles.
2. **Main Responsibilities:** Twin registration, autonomy level assignment (`ASSISTIVE`, `SUPERVISED`, `AUTONOMOUS`), permission rule configuration, active policy configuration lock, Admin status management.
3. **Technology and Persistence:** Java 17, Spring Boot 3.4.3, Spring WebFlux, R2DBC + H2 (`aitwindb`).
4. **Important Packages/Components:** `org.example.aitwinservice.service.AiTwinService`, `org.example.aitwinservice.controller.AiTwinController`, `org.example.aitwinservice.repo.AiTwinRepo`.
5. **Main Domain Entities:** `AiTwin` (twinId, customerId, name, autonomyLevel, transactionLimit, approvalThreshold, status, isPolicyLocked), `AiTwinPermission` (permissionId, twinId, actionType, permissionLevel, actionLimit).
6. **Main DTO Categories:** `AiTwinCreateRequestDTO`, `AiTwinResponseDTO`, `AiTwinRiskProfileResponseDTO`, `PermissionCreateRequestDTO`.
7. **Main API Groups:** `/api/ai-twins`, `/api/ai-twins/{twinId}`, `/api/ai-twins/{twinId}/permissions`, `/api/ai-twins/{twinId}/risk-profile`.
8. **Authentication and Authorization:** Secured via Gateway. Customers access owned twins; Underwriters call `/risk-profile`; Admins manage all twins.
9. **Cross-Service Dependencies:** Underwriting evaluation in `insurance-policy-service` fetches AI Twin risk profile from this service.
10. **Important Validations:** Financial control validation (approval threshold cannot exceed transaction limit), active policy configuration lock checks.
11. **Important Exceptions:** `AiTwinNotFoundException`, `PolicyConfigurationLockedException`, `InvalidFinancialControlException`.
12. **Logging/Monitoring/Actuator Support:** SLF4J logging for twin creation and permission changes.
13. **Database Ownership:** Isolated `aitwindb` tables `ai_twins` and `ai_twin_permissions`.
14. **Existing Tests:** `AiTwinServiceApplicationTests.java`.
15. **Tests Added:** None executed due to missing `spring-boot-starter-test` dependency in `pom.xml`.
16. **Testing Limitations:** Requires adding `spring-boot-starter-test` and `reactor-test` to `pom.xml` for full unit testing.
17. **Known Limitations:** In-memory H2 database; permissions require separate reactive repository queries.
18. **Key Configuration Requirements:** `server.port=8083`, `spring.r2dbc.url=r2dbc:h2:mem:///aitwindb`.
