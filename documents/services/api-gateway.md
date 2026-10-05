# API Gateway Documentation

1. **Purpose:** Single entry point for all frontend client traffic, handling request routing, CORS headers, rate limiting, and centralized JWT cookie validation.
2. **Main Responsibilities:** Routes `/api/auth/**` to `identity-service`, `/api/customers/**` to `customer-service`, `/api/ai-twins/**` to `ai-twin-service`, `/api/products/**`, `/api/policy-applications/**`, `/api/policies/**` to `insurance-policy-service`, `/api/ai-actions/**` to `ai-action-service`, `/api/incidents/**`, `/api/claims/**` to `claims-service`, `/api/notifications/**` to `notification-service`.
3. **Technology and Persistence:** Java 17, Spring Boot 3.4.3, Spring Cloud Gateway WebFlux, Spring Security Reactive. No database.
4. **Important Packages/Components:** `org.example.apigateway.config.SecurityConfig`, `org.example.apigateway.filter.JwtAuthenticationFilter`.
5. **Main Domain Entities:** None (Stateless Gateway).
6. **Main DTO Categories:** None (Pass-through Routing & Filter Context).
7. **Main API Groups:** All `/api/**` dynamic gateway routes.
8. **Authentication and Authorization:** Extracts signed JWT from incoming `JWT-TOKEN` HttpOnly cookie or `Authorization: Bearer` header, validates signature using shared secret, and injects user identity headers into downstream requests.
9. **Cross-Service Dependencies:** Integrates with `eureka-server` for dynamic `lb://SERVICE-NAME` URI resolution.
10. **Important Validations:** Valid JWT signature, unexpired token timestamp, allowed CORS origins (`http://localhost:4200`).
11. **Important Exceptions:** Returns `401 Unauthorized` for invalid/missing JWT, `403 Forbidden` for role mismatch.
12. **Logging/Monitoring/Actuator Support:** SLF4J gateway route logging; Spring Boot Actuator enabled (`/actuator/gateway/routes`).
13. **Database Ownership:** None.
14. **Existing Tests:** `ApiGatewayApplicationTests.java`.
15. **Tests Added:** Gateway filter security test suite.
16. **Testing Limitations:** Testing route forwarding requires running embedded Eureka discovery.
17. **Known Limitations:** In-memory route table relies on Eureka service registration heartbeats.
18. **Key Configuration Requirements:** `server.port=8080`, `eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka`.
