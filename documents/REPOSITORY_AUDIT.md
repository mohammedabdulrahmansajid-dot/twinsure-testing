# TwinSure Repository Audit & Investigation Report

**Classification:** Non-Confidential | **Project:** TwinSure Microservices & Angular Platform  
**Date:** October 5, 2026 | **Branch:** `testing-ui-documentation`

---

## 1. Executive Summary & Audit Overview

This audit documents the state of the TwinSure capstone platform prior to testing, UI polishing, and documentation tasks. The repository is a full-stack polyglot reactive microservice system comprising 9 Spring Boot backend services and 1 Angular single-page application.

---

## 2. Microservices & Backend Inventory

| Microservice Name | Build System | Java Version | Spring Boot Version | Database / Persistence | Test Dependencies | Testing Capability | Existing Tests |
|---|---|---|---|---|---|---|---|
| `eureka-server` | Maven (`mvnw.cmd`) | Java 17 | 3.4.3 | N/A (In-Memory Discovery Registry) | `spring-boot-starter-test` | **PARTIAL / READY (Minimal)** | `EurekaServerApplicationTests.java` |
| `api-gateway` | Maven (`mvnw.cmd`) | Java 17 | 3.4.3 | N/A (Reactive Netty Routing & Security) | `spring-boot-starter-test`, `spring-boot-starter-security-test`, `reactor-test` | **READY** | `ApiGatewayApplicationTests.java` |
| `identity-service` | Maven (`mvnw.cmd`) | Java 17 | 3.4.3 | H2 (R2DBC Reactive) | `spring-boot-starter-test` (`reactor-test` commented out) | **PARTIAL** | `IdentityServiceApplicationTests.java` |
| `customer-service` | Maven (`mvnw.cmd`) | Java 17 | 3.4.3 | H2 (R2DBC Reactive) | None in `pom.xml` | **NOT READY** | `CustomerServiceApplicationTests.java` |
| `ai-twin-service` | Maven (`mvnw.cmd`) | Java 17 | 3.4.3 | H2 (R2DBC Reactive) | None in `pom.xml` | **NOT READY** | `AiTwinServiceApplicationTests.java` |
| `insurance-policy-service` | Maven (`mvnw.cmd`) | Java 17 | 3.4.3 | H2 (R2DBC Reactive) | `spring-boot-starter-test`, `spring-boot-starter-data-r2dbc-test`, `spring-boot-starter-security-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webflux-test`, `reactor-test` | **READY** | `InsurancePolicyServiceApplicationTests.java`, `UnderwritingCalculatorTest.java` |
| `ai-action-service` | Maven (`mvnw.cmd`) | Java 17 | 3.4.3 | H2 (R2DBC Reactive) | `spring-boot-starter-test`, `spring-boot-starter-data-r2dbc-test`, `spring-boot-starter-security-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webflux-test`, `reactor-test` | **READY** | `AiActionServiceApplicationTests.java`, `ActionRuleEvaluatorTest.java` |
| `claims-service` | Maven (`mvnw.cmd`) | Java 17 | 3.4.3 | H2 (R2DBC Reactive) | `spring-boot-starter-test`, `spring-boot-starter-data-r2dbc-test`, `spring-boot-starter-security-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webflux-test`, `reactor-test` | **READY** | `ClaimsServiceApplicationTests.java`, `ClaimServiceTest.java` |
| `notification-service` | Maven (`mvnw.cmd`) | Java 17 | 3.4.3 | H2 (R2DBC Reactive) | `spring-boot-starter-test`, `spring-boot-starter-data-r2dbc-test`, `spring-boot-starter-security-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webflux-test`, `reactor-test` | **READY** | `NotificationServiceApplicationTests.java`, `NotificationServiceTest.java` |

---

## 3. Services Lacking Sufficient Testing Dependencies

1. **`customer-service`**: Missing `spring-boot-starter-test`, `reactor-test`, `spring-security-test` in `pom.xml`.
2. **`ai-twin-service`**: Missing `spring-boot-starter-test`, `reactor-test`, `spring-security-test` in `pom.xml`.
3. **`identity-service`**: Has `spring-boot-starter-test`, but `reactor-test` is commented out in `pom.xml`.

*Policy Rule Enforced:* Per safety rules, missing dependencies in `customer-service` and `ai-twin-service` will not be added silently. Only tests compiling with the present pom dependencies are executed, and missing dependencies are documented with exact XML snippets for future inclusion.

---

## 4. Frontend Architecture & Setup (`twinsure-frontend`)

- **Angular Version:** Angular 21 (v21.2.0) with Standalone Components & `@angular/build:application`
- **Build & Test System:** Vitest (`vitest` 5.0.3, `jsdom` 28.0.0) configured via `@angular/build:unit-test`
- **Styling:** Tailwind CSS (v4.1.12) with custom PostCSS configuration
- **NgRx Features Installed:**
  - `@ngrx/store` (v21.1.1)
  - `@ngrx/effects` (v21.1.1)
  - `@ngrx/entity` (v21.1.1)
  - `@ngrx/router-store` (v21.1.1)
  - `@ngrx/store-devtools` (v21.1.1)
- **Current NgRx Usage:**
  - State management uses local signals (`signal`, `computed`, `effect`) across components and feature services, with NgRx Store registered in `app.config.ts`.
- **Frontend Route Structure:**
  - **Public Routes:** `/` (Welcome), `/register` (Register), `/login` (Login), `/unauthorized` (Access Denied)
  - **Protected Shell (`/` -> `AppLayout`):**
    - **Customer:** `/customer/dashboard`, `/customer/profile`, `/customer/ai-twins`, `/customer/ai-twins/new`, `/customer/ai-twins/:twinId`, `/customer/insurance-products`, `/customer/insurance-products/:productId`, `/customer/policy-applications`, `/customer/policy-applications/new`, `/customer/policy-applications/:applicationId`, `/customer/policies`, `/customer/policies/:policyId`, `/customer/ai-actions`, `/customer/ai-actions/simulate`, `/customer/ai-actions/:actionId`, `/customer/incidents`, `/customer/incidents/new`, `/customer/incidents/:incidentId`, `/customer/claims`, `/customer/claims/new`, `/customer/claims/:claimId`
    - **Underwriter:** `/underwriter/dashboard`, `/underwriter/applications/pending`, `/underwriter/applications/reviewed`, `/underwriter/applications/:applicationId`
    - **Claims Adjuster:** `/claims-adjuster/dashboard`, `/claims-adjuster/claims`, `/claims-adjuster/claims/:claimId`
    - **Admin:** `/admin` (nested lazy child routes: users, customers, ai-twins, products, policies, applications, actions, incidents, claims)
    - **Shared:** `/notifications`

---

## 5. Security, Authentication & Authorization Model

- **Authentication Issuer:** `identity-service` (port 8081 via Gateway 8080)
- **Session Transport:** HttpOnly Cookie containing signed JWT (`JWT-TOKEN`)
- **Frontend Security:** JavaScript does NOT read or store JWTs in localStorage/sessionStorage. HTTP requests rely on `withCredentials: true` via Angular HTTP interceptor (`auth.interceptor.ts`).
- **Guards:**
  - `authGuard`: Ensures authenticated user session before accessing `/customer/*`, `/underwriter/*`, `/claims-adjuster/*`, `/admin/*`.
  - `guestGuard`: Prevents logged-in users from accessing `/login` or `/register`.
  - `roleGuard`: Functional guard parameterizing `APPLICATION_ROLES` (`CUSTOMER`, `UNDERWRITER`, `CLAIMS_ADJUSTER`, `ADMIN`).

---

## 6. Machine & Build Limitations

- **Java Version:** 26.0.2 (JDK 26 installed).
- **Maven Global Executable:** `mvn` command not found in global system PATH.
- **Maven Wrapper:** `mvnw.cmd` is present in every backend service folder.
- **Node.js Version:** 24.11.0 (v24.11.0).
- **npm Version:** 11.12.1 (v11.12.1).
- **Database Server:** In-memory H2 (R2DBC reactive driver `r2dbc-h2`) per service; no external PostgreSQL or MySQL container instance running.

---

## 7. Current Project Documentation

- `README.md` in root (basic overview)
- `Documentation for Capstone project.xlsx` in root
- `twinsure-frontend/README.md`
