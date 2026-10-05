# Claims Service Documentation

1. **Purpose:** Manages the end-to-end claim lifecycle from initial incident reporting to claim filing, evidence submission, adjuster investigation, decision recording, and financial payout approval.
2. **Main Responsibilities:** Incident reporting, claim creation with duplicate filing protection, evidence management, Claims Adjuster assignment, review status transitions (`SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `PARTIALLY_APPROVED`, `REJECTED`, `CLOSED`), decision auditing.
3. **Technology and Persistence:** Java 17, Spring Boot 3.4.3, Spring WebFlux, R2DBC + H2 (`claimsdb`), Load-balanced WebClient.
4. **Important Packages/Components:** `org.example.claimsservice.service.ClaimService`, `org.example.claimsservice.service.IncidentService`, `org.example.claimsservice.controller.ClaimController`.
5. **Main Domain Entities:** `Incident`, `Claim`, `ClaimEvidence`, `ClaimDecision`.
6. **Main DTO Categories:** `IncidentCreateRequestDTO`, `ClaimCreateRequestDTO`, `ClaimEvidenceRequestDTO`, `ClaimDecisionRequestDTO`, `ClaimResponseDTO`.
7. **Main API Groups:** `/api/incidents`, `/api/claims`, `/api/claims/{claimId}/evidence`, `/api/claims/{claimId}/decision`, `/api/claims/admin/assign`.
8. **Authentication and Authorization:** Secured via Gateway. Customers file incidents and claims; Claims Adjusters review assigned claims; Admins assign adjusters.
9. **Cross-Service Dependencies:** Invokes `notification-service` to notify customers when claim decisions or status updates occur.
10. **Important Validations:** Duplicate claim prevention per incident, approved amount cannot exceed claimed amount or coverage limit, evidence URL non-empty.
11. **Important Exceptions:** `ClaimNotFoundException`, `DuplicateClaimException`, `UnauthorizedAdjusterException`, `InvalidClaimAmountException`.
12. **Logging/Monitoring/Actuator Support:** SLF4J audit logging for financial decisions; Spring Boot Actuator enabled.
13. **Database Ownership:** Isolated `claimsdb` tables `incidents`, `claims`, `claim_evidences`, `claim_decisions`.
14. **Existing Tests:** `ClaimsServiceApplicationTests.java`, `ClaimServiceTest.java`.
15. **Tests Added:** Executed 15 passing tests in `ClaimServiceTest`.
16. **Testing Limitations:** Integration tests require mocking `notification-service` WebClient endpoint.
17. **Known Limitations:** Document file storage uses URL links rather than local S3/Blob storage.
18. **Key Configuration Requirements:** `server.port=8086`, `spring.r2dbc.url=r2dbc:h2:mem:///claimsdb`.
