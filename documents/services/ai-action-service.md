# AI Action Service Documentation

1. **Purpose:** Evaluates AI Twin actions in real time against active policy terms, permissions, and financial limits.
2. **Main Responsibilities:** Action simulation, rule evaluation (`ActionRuleEvaluator`), violation detection, claim-eligibility tagging, compliance logging.
3. **Technology and Persistence:** Java 17, Spring Boot 3.4.3, Spring WebFlux, R2DBC + H2 (`aiactiondb`).
4. **Important Packages/Components:** `org.example.aiactionservice.service.ActionRuleEvaluator`, `org.example.aiactionservice.service.AiActionService`, `org.example.aiactionservice.controller.AiActionController`.
5. **Main Domain Entities:** `AiAction` (actionId, twinId, policyId, customerId, actionType, amount, complianceStatus, timestamp), `ActionViolation` (violationId, actionId, violationType, isClaimEligible).
6. **Main DTO Categories:** `ActionSimulationRequestDTO`, `ActionSimulationResponseDTO`, `ViolationDTO`.
7. **Main API Groups:** `/api/ai-actions/simulate`, `/api/ai-actions/{actionId}`, `/api/ai-actions/history`.
8. **Authentication and Authorization:** Secured via Gateway. Customers simulate actions for owned twins/policies; Admins view all action logs.
9. **Cross-Service Dependencies:** Reads twin permissions from `ai-twin-service` and coverage boundaries from `insurance-policy-service`.
10. **Important Validations:** Action amount non-negative, active policy binding verification, allowed action type checking.
11. **Important Exceptions:** `ActionSimulationException`, `PolicyNotActiveException`, `InvalidActionPayloadException`.
12. **Logging/Monitoring/Actuator Support:** SLF4J logging for action compliance and violations; Spring Boot Actuator enabled.
13. **Database Ownership:** Isolated `aiactiondb` tables `ai_actions` and `action_violations`.
14. **Existing Tests:** `AiActionServiceApplicationTests.java`, `ActionRuleEvaluatorTest.java`.
15. **Tests Added:** `ActionRuleEvaluatorTest` verifying compliance scoring and claim-eligibility flags.
16. **Testing Limitations:** Requires mocking external WebClient responses for Policy and Twin lookups.
17. **Known Limitations:** In-memory H2 persistence; real-time event streaming not currently enabled.
18. **Key Configuration Requirements:** `server.port=8085`, `spring.r2dbc.url=r2dbc:h2:mem:///aiactiondb`.
