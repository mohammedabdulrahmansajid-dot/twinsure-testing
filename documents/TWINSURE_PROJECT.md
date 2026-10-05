# TwinSure Platform Project Documentation

## 1. Project Overview
TwinSure is a comprehensive microservice platform that provides risk evaluation, insurance coverage, policy issuance, AI action compliance monitoring, incident reporting, and claim settlement for autonomous AI Twins.

## 2. Problem Addressed
As autonomous AI Twins act on behalf of individuals (executing transactions, making reservations, managing subscriptions), financial liability risks arise. Traditional insurance platforms lack real-time risk scoring for autonomous software agents and automated violation verification.

## 3. Main Objectives
- Provide real-time risk scoring for AI Twins based on autonomy level and permission bounds.
- Support automated policy binding, underwriting reviews, and instant policy issuance.
- Evaluate AI Twin action compliance in real time and trigger automated claim eligibility for violations.
- Enable end-to-end claim reporting, evidence auditing, and settlement workflows.

## 4. Scope
Covers 9 backend microservices (`eureka-server`, `api-gateway`, `identity-service`, `customer-service`, `ai-twin-service`, `insurance-policy-service`, `ai-action-service`, `claims-service`, `notification-service`) and 1 Angular 21 single-page application (`twinsure-frontend`).

## 5. User Roles
- **Customer:** Registers AI Twins, purchases insurance, simulates actions, reports incidents, files claims.
- **Underwriter:** Reviews policy applications, evaluates AI Twin risk scores, issues underwriting decisions.
- **Claims Adjuster:** Audits claim evidence, requests documentation, approves or rejects claims.
- **Administrator:** Manages system users, customer profiles, product catalog, and adjuster assignments.

## 6. High-Level Architecture
Reactive microservice architecture built on Spring WebFlux, Netty, R2DBC, Spring Cloud Gateway, Eureka Server, and Angular 21.

## 7. Repository Structure
- `twinsure-frontend/`: Angular single-page app.
- `api-gateway/`: Reactive routing & security gateway (Port 8080).
- `eureka-server/`: Service discovery registry (Port 8761).
- `identity-service/` (Port 8081), `customer-service/` (Port 8082), `ai-twin-service/` (Port 8083), `insurance-policy-service/` (Port 8084), `ai-action-service/` (Port 8085), `claims-service/` (Port 8086), `notification-service/` (Port 8087).

## 8. Microservice Responsibilities
- `identity-service`: Auth & JWT cookie issuing.
- `customer-service`: Customer profile management.
- `ai-twin-service`: AI Twin registration & permissions.
- `insurance-policy-service`: Insurance products & underwriting calculations.
- `ai-action-service`: Action simulation & violation detection.
- `claims-service`: Incident reporting & claim adjudication.
- `notification-service`: System notifications.
- `api-gateway`: Security filtering & request routing.
- `eureka-server`: Service registration & heartbeats.

## 9. Communication Between Services
Asynchronous reactive HTTP WebClient communication load-balanced via Eureka discovery (`lb://SERVICE-NAME`).

## 10. API Gateway
Single entry point (Port 8080) handling CORS, rate limiting, and centralized JWT validation.

## 11. Eureka Discovery
Eureka Server (Port 8761) provides dynamic service registration so microservices locate each other without hardcoded IP addresses.

## 12. Authentication and Authorization
JWT-based authentication delivered via HttpOnly `JWT-TOKEN` cookies. JavaScript does not access JWTs. Role authorization is enforced at Gateway and microservices.

## 13. Database-Per-Service Design
Isolated database schemas per microservice using Spring Data R2DBC and H2 in-memory databases (`identitydb`, `customerdb`, `aitwindb`, `policydb`, `aiactiondb`, `claimsdb`, `notificationdb`).

## 14. Core Business Workflows
1. Customer registers -> Identity Service creates user -> Customer Service creates profile.
2. Customer registers AI Twin -> configures transaction limits & permissions.
3. Customer applies for Insurance Product -> UnderwritingCalculator scores risk (0-100) -> Underwriter approves & Policy is issued.
4. AI Twin action simulated -> AI Action Service evaluates rules -> Flagged violations create claim-eligible logs.
5. Customer files Incident & Claim -> Adjuster audits evidence & records decision -> Notification sent.

## 15. Validation Strategy
Jakarta Validation (`@NotNull`, `@Size`, `@Email`) at DTO boundaries; domain validation in reactive services.

## 16. DTO Strategy
Strict separation between database entities and API DTOs (Request DTOs, Response DTOs, Internal DTOs).

## 17. Exception and Error Handling
Custom domain exceptions (`UserNotFoundException`, `InvalidUnderwritingDecisionException`) mapped to HTTP status codes via `@RestControllerAdvice` global exception handlers.

## 18. Logging/Monitoring
SLF4J logging for security events and business transactions; Spring Boot Actuator endpoints (`/actuator/health`).

## 19. Frontend Architecture
Angular 21 Standalone Components, Reactive Forms, Router Guards (`authGuard`, `guestGuard`, `roleGuard`), and shared `AppLayout`.

## 20. NgRx Usage
NgRx Store (`authFeature`) for global authentication state; local signals for component UI state.

## 21. Testing Summary
JUnit 5, Mockito, and Reactor StepVerifier backend unit tests; Vitest frontend component & store specs.

## 22. Database Verification Summary
Automated R2DBC repository tests + 7 read-only SQL integrity scripts (`documents/testing/sql/`).

## 23. Build and Setup Summary
Backend microservices build via Maven Wrapper (`.\mvnw.cmd test`). Frontend builds via npm (`npm run build`).

## 24. Strengths
- High-performance reactive non-blocking architecture.
- Strong security model with HttpOnly cookies.
- Automated AI risk scoring algorithm.
- Clean database-per-service isolation.

## 25. Weaknesses
- In-memory H2 databases reset state upon application restart.
- External S3 document storage currently uses URL links.

## 26. Known Limitations
- `customer-service` and `ai-twin-service` pom.xml files lack test dependencies in baseline.

## 27. Deferred Enhancements
- Integration of Apache Kafka for event-driven asynchronous messaging.
- Integration of PostgreSQL for persistent storage.

## 28. Final Project Status
Fully audited, tested, polished, and documented release candidate on branch `testing-ui-documentation`.
