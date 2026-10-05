# TwinSure API Overview

All requests are routed through API Gateway at `http://localhost:8080`.

| Endpoint Path | Method | Access Role | Description |
|---|---|---|---|
| `/api/auth/register` | POST | Public | Register customer account. |
| `/api/auth/login` | POST | Public | Authenticate user & issue HttpOnly cookie. |
| `/api/auth/logout` | POST | Authenticated | Invalidate session. |
| `/api/customers/me` | GET | Customer | Fetch logged-in customer profile. |
| `/api/ai-twins` | GET/POST | Customer | List or register AI Twins. |
| `/api/ai-twins/{id}/risk-profile` | GET | Underwriter/Admin | Fetch AI Twin risk configuration. |
| `/api/products` | GET | Public/Customer | List active insurance products. |
| `/api/policy-applications` | POST | Customer | Apply for insurance product. |
| `/api/policy-applications/{id}/underwrite` | POST | Underwriter | Submit underwriting decision. |
| `/api/ai-actions/simulate` | POST | Customer | Simulate AI action & evaluate rules. |
| `/api/incidents` | POST | Customer | Report AI Twin incident. |
| `/api/claims` | POST | Customer | File insurance claim. |
| `/api/claims/{id}/decision` | POST | Claims Adjuster | Record claim approval/rejection. |
| `/api/notifications` | GET | Authenticated | Retrieve user notifications. |
