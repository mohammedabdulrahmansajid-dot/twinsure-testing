# TwinSure Database Architecture

## Database-per-Service Design
Each microservice maintains strict schema isolation using Spring Data R2DBC over H2 reactive driver (`r2dbc-h2`):

- `identity-service`: `identitydb` (`users`)
- `customer-service`: `customerdb` (`customers`)
- `ai-twin-service`: `aitwindb` (`ai_twins`, `ai_twin_permissions`)
- `insurance-policy-service`: `policydb` (`insurance_products`, `policy_applications`, `policies`, `coverages`, `exclusions`)
- `ai-action-service`: `aiactiondb` (`ai_actions`, `action_violations`)
- `claims-service`: `claimsdb` (`incidents`, `claims`, `claim_evidences`, `claim_decisions`)
- `notification-service`: `notificationdb` (`notifications`)

Read-only SQL verification scripts are provided under `documents/testing/sql/`.
