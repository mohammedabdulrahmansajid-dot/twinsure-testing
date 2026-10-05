# TwinSure Security & Authentication Model

## 1. Authentication Strategy
- **JWT Issuance:** Issued by `identity-service` upon successful login.
- **HttpOnly Cookie Transport:** Token is stored in `JWT-TOKEN` HttpOnly cookie.
- **Zero Frontend Storage:** JavaScript cannot access JWT tokens in localStorage or sessionStorage.

## 2. Authorization & Role-Based Access Control (RBAC)
- Roles: `CUSTOMER`, `UNDERWRITER`, `CLAIMS_ADJUSTER`, `ADMIN`.
- Microservice Security: Spring Security Reactive `ServerHttpSecurity` filters enforce endpoint permissions.

## 3. Data Protection & Input Sanitization
- Passwords encoded via BCrypt (`PasswordEncoder`).
- All DTOs validated via Jakarta Validation annotations (`@NotBlank`, `@Size`, `@Email`).
