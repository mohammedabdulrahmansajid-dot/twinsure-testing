# TwinSure Database Testing & Verification Report

**Classification:** Non-Confidential | **Project:** TwinSure Database-Per-Service Architecture  
**Date:** October 5, 2026 | **Branch:** `testing-ui-documentation`

---

## 1. Database & Persistence Architecture Overview

Each microservice in the TwinSure platform owns an isolated, dedicated database schema using Spring Data R2DBC over H2 reactive driver (`r2dbc-h2`):

| Microservice | Technology | Database Name / Target | Key Constraint & Integrity Rules |
|---|---|---|---|
| `identity-service` | R2DBC + H2 | `identitydb` | Unique `username`, Role enum validation, password hash non-null. |
| `customer-service` | R2DBC + H2 | `customerdb` | Unique `user_id`, Unique `email`, valid customer status. |
| `ai-twin-service` | R2DBC + H2 | `aitwindb` | Foreign key `customer_id`, autonomy level enum, policy lock flag. |
| `insurance-policy-service` | R2DBC + H2 | `policydb` | Unique `product_code`, application status state transitions, underwriting snapshot. |
| `ai-action-service` | R2DBC + H2 | `aiactiondb` | Action simulation compliance log, claim eligibility flag. |
| `claims-service` | R2DBC + H2 | `claimsdb` | Unique `incident_id` claim constraint, adjuster assignment ID, approved amount limit. |
| `notification-service` | R2DBC + H2 | `notificationdb` | Recipient `user_id` ownership, unread flag counter. |

---

## 2. Automated R2DBC Database Tests

In microservices configured with `spring-boot-starter-data-r2dbc-test` (`insurance-policy-service`, `claims-service`, `ai-action-service`, `notification-service`), database unit & repository tests run directly against the embedded R2DBC H2 engine in memory without requiring external docker containers or local server setup.

---

## 3. Read-Only Database Verification Scripts

Safe, read-only SQL scripts have been created under `documents/testing/sql/` to audit schema state, detect orphan records, and verify domain relationships:

1. `documents/testing/sql/identity-integrity-checks.sql`
2. `documents/testing/sql/customer-integrity-checks.sql`
3. `documents/testing/sql/ai-twin-integrity-checks.sql`
4. `documents/testing/sql/insurance-policy-integrity-checks.sql`
5. `documents/testing/sql/ai-action-integrity-checks.sql`
6. `documents/testing/sql/claims-integrity-checks.sql`
7. `documents/testing/sql/notification-integrity-checks.sql`

---

## 4. Execution & Safety Instructions

- **Safety Status:** All 7 SQL scripts are strictly `READ-ONLY` `SELECT` queries. They perform zero data mutation or deletion (`INSERT`, `UPDATE`, `DELETE`, `DROP` are prohibited).
- **Execution Method:** Can be safely executed against H2 Web Console at `http://localhost:<service-port>/h2-console` or via R2DBC test runner.
