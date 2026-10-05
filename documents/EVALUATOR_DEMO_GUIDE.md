# TwinSure Comprehensive Evaluator & Demonstration Guide

**Project:** TwinSure Full-Stack AI Twin Insurance Microservice Platform  
**Target Environment:** IntelliJ IDEA / Terminal / Browser  
**Classification:** Non-Confidential Evaluation & Verification Guide  
**Branch:** `testing-ui-documentation`

---

## 1. Executive Summary & Evaluator Presentation Pitch

TwinSure is a reactive microservice platform engineered for next-generation AI agents and autonomous digital twins. As AI twins perform real-world actions (purchases, bookings, subscription management), TwinSure dynamically calculates underwriting risk scores, issues bound insurance policies, monitors action telemetry in real time, and handles end-to-end incident claims.

### Key Technical Highlights to Pitch:
- **Reactive Stack:** Non-blocking Java 17, Spring Boot 3.4.3, Spring WebFlux, Spring Data R2DBC, and Netty.
- **Microservices & Gateway:** 9 decoupled microservices managed by Netflix Eureka discovery and Spring Cloud API Gateway (Port 8080).
- **Enterprise Security Model:** `identity-service` issues signed JWTs transported exclusively via HttpOnly `JWT-TOKEN` cookies (preventing XSS access in JavaScript).
- **Frontend Architecture:** Angular 21 Standalone Components with Signals, NgRx Store, Reactive Forms, and Tailwind CSS.
- **Database Isolation:** Database-per-service architecture using Spring Data R2DBC over embedded H2 engines.

---

## 2. IntelliJ IDEA Demonstration Setup & Execution Guide

### Step 1: Opening the Project in IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Select **File -> Open...** and select the repository root folder:  
   `twinsure-testing`
3. IntelliJ will automatically detect Maven modules for all 9 microservices.

### Step 2: Running Microservices from IntelliJ IDEA
You can start microservices from the IntelliJ **Services Tool Window** or run configurations in order:

1. **Start Eureka Server:**
   - Navigate to `eureka-server/src/main/java/org/example/eurekaserver/EurekaServerApplication.java`
   - Right-click and select **Run 'EurekaServerApplication'** (Listens on `http://localhost:8761`).
   - Open browser to `http://localhost:8761` to show the active Eureka Discovery Dashboard.

2. **Start API Gateway:**
   - Navigate to `api-gateway/src/main/java/org/example/apigateway/ApiGatewayApplication.java`
   - Right-click and select **Run 'ApiGatewayApplication'** (Listens on `http://localhost:8080`).

3. **Start Core Microservices:**
   - Run `IdentityServiceApplication.java` (Port 8081)
   - Run `CustomerServiceApplication.java` (Port 8082)
   - Run `AiTwinServiceApplication.java` (Port 8083)
   - Run `InsurancePolicyServiceApplication.java` (Port 8084)
   - Run `AiActionServiceApplication.java` (Port 8085)
   - Run `ClaimsServiceApplication.java` (Port 8086)
   - Run `NotificationServiceApplication.java` (Port 8087)

---

## 3. How to Show Unit Tests & Code Coverage Reports

### A. Showing Coverage Reports in IntelliJ IDEA
1. In the **Project Tool Window**, right-click any microservice folder (e.g. `insurance-policy-service`, `claims-service`, `identity-service`).
2. Select **Run 'Tests in...' with Coverage** (or click the green shield icon in the top toolbar).
3. IntelliJ IDEA will execute all JUnit 5 / Reactor StepVerifier unit tests and display the **Coverage Tool Window** with percentage metrics for classes, methods, and lines.

### B. Running Backend Unit Tests via Command Line
To run unit tests from the terminal:
```bash
# Identity Service
cd identity-service
.\mvnw.cmd test

# Insurance Policy Service
cd insurance-policy-service
.\mvnw.cmd test

# Claims Service
cd claims-service
.\mvnw.cmd test

# AI Action Service
cd ai-action-service
.\mvnw.cmd test
```

### C. Running Frontend Unit Tests
To run Angular 21 unit tests:
```bash
cd twinsure-frontend
npx vitest run
```

---

## 4. How to Perform & Demonstrate Database Testing

### A. Database Architecture
Explain to the evaluator that TwinSure uses a **Database-Per-Service** architecture using Spring Data R2DBC over H2 (`identitydb`, `customerdb`, `aitwindb`, `policydb`, `aiactiondb`, `claimsdb`, `notificationdb`).

### B. Running Read-Only Database Integrity Checks
Demonstrate data integrity by showcasing the 7 prepared read-only SQL scripts under `documents/testing/sql/`:

1. Open **IntelliJ Database Tool Window** or navigate to H2 Console (`http://localhost:8081/h2-console`).
2. Show the evaluator the read-only integrity scripts:
   - [`documents/testing/sql/identity-integrity-checks.sql`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/documents/testing/sql/identity-integrity-checks.sql) (Audit duplicate usernames & customer links)
   - [`documents/testing/sql/customer-integrity-checks.sql`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/documents/testing/sql/customer-integrity-checks.sql) (Audit duplicate emails & user IDs)
   - [`documents/testing/sql/ai-twin-integrity-checks.sql`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/documents/testing/sql/ai-twin-integrity-checks.sql) (Audit autonomy levels & policy locks)
   - [`documents/testing/sql/insurance-policy-integrity-checks.sql`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/documents/testing/sql/insurance-policy-integrity-checks.sql) (Audit product codes & underwriting snapshots)
   - [`documents/testing/sql/ai-action-integrity-checks.sql`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/documents/testing/sql/ai-action-integrity-checks.sql) (Audit action compliance & claim eligibility)
   - [`documents/testing/sql/claims-integrity-checks.sql`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/documents/testing/sql/claims-integrity-checks.sql) (Audit duplicate claim protection & approved limits)
   - [`documents/testing/sql/notification-integrity-checks.sql`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/documents/testing/sql/notification-integrity-checks.sql) (Audit recipient unread counts)

---

## 5. Demonstration of Required Evaluation Rubrics

Walk the evaluator through the project using this rubric cheat-sheet:

| Rubric Area | What to Show in Code / UI | File Reference |
|---|---|---|
| **Architecture & Microservices** | Eureka Discovery dashboard on Port 8761 and API Gateway routing configuration | [`api-gateway/pom.xml`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/api-gateway/pom.xml) |
| **JWT & Security Model** | HttpOnly `JWT-TOKEN` cookie handling; explain JavaScript never reads raw JWTs | [`JwtUtil.java`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/identity-service/src/main/java/org/example/identityservice/utility/JwtUtil.java) |
| **Frontend State Management** | NgRx Store (`authFeature`) for global auth + Signals for UI state | [`auth.reducer.ts`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/twinsure-frontend/src/app/features/auth/state/auth.reducer.ts) |
| **Frontend Form Validation** | Angular Reactive Forms with required, min/max length, and business rules | [`register.ts`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/twinsure-frontend/src/app/features/auth/pages/register/register.ts) |
| **Underwriting Algorithm** | Dynamic risk scoring (0-100) based on twin autonomy & permission limits | [`UnderwritingCalculator.java`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/insurance-policy-service/src/main/java/org/example/insurancepolicyservice/service/UnderwritingCalculator.java) |
| **Action Simulation & Rules** | Real-time rule evaluation & claim-eligibility tagging | [`ActionRuleEvaluator.java`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/ai-action-service/src/main/java/org/example/aiactionservice/service/ActionRuleEvaluator.java) |
| **Claim Lifecycle** | Incident creation -> Claim submission -> Evidence audit -> Adjuster decision | [`ClaimService.java`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/claims-service/src/main/java/org/example/claimsservice/service/ClaimService.java) |
| **Production UI Polish** | Clean landing page with **Sign in** and **Create account** CTAs without dev labels | [`welcome.html`](file:///c:/Users/abdul/Desktop/projects/Twin%20Sure/twinsure-testing/twinsure-frontend/src/app/features/welcome/pages/welcome/welcome.html) |

---

## 6. Engineering Best Practices Applied

1. **Decoupled Architecture:** Strict separation between microservices with database-per-service isolation.
2. **Reactive Non-blocking I/O:** Complete reactive pipeline from Spring WebFlux controllers down to R2DBC drivers.
3. **Defense in Depth Security:** HttpOnly cookies for session transport combined with role-based API Gateway guards.
4. **Clean Code & DTO Strategy:** Immutability with Java Records and TypeScript interfaces; no sensitive password hash leakage.
5. **Robust Exception Handling:** Centralized `@RestControllerAdvice` mapping domain exceptions to HTTP status codes.

---

## 7. Key Learnings & Technical Insights

1. **Reactive State Management with R2DBC:** Reactive databases require reactive operators (`Mono.flatMap`, `Flux.map`) instead of traditional blocking JPA/Hibernate calls.
2. **HttpOnly Cookie Interception:** Configuring Angular HTTP interceptors with `withCredentials: true` provides superior security against XSS token theft compared to local storage.
3. **Decoupled Service Discovery:** Netflix Eureka enables seamless service lookup without hardcoded host addresses, simplifying containerization and local development.
