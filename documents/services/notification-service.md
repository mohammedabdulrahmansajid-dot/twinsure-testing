# Notification Service Documentation

1. **Purpose:** Generates, stores, and delivers in-app notifications to system users across all roles.
2. **Main Responsibilities:** Notification creation, user recipient ownership enforcement, unread notification counter, mark-as-read updates, customer/staff message retrieval.
3. **Technology and Persistence:** Java 17, Spring Boot 3.4.3, Spring WebFlux, R2DBC + H2 (`notificationdb`).
4. **Important Packages/Components:** `org.example.notificationservice.service.NotificationService`, `org.example.notificationservice.controller.NotificationController`, `org.example.notificationservice.repo.NotificationRepo`.
5. **Main Domain Entities:** `Notification` (notificationId, userId, title, message, category, isRead, createdAt).
6. **Main DTO Categories:** `NotificationCreateRequestDTO`, `NotificationResponseDTO`, `UnreadCountResponseDTO`.
7. **Main API Groups:** `/api/notifications`, `/api/notifications/unread-count`, `/api/notifications/{id}/read`.
8. **Authentication and Authorization:** Secured via Gateway. Users fetch and modify only notifications matching their authenticated `userId`.
9. **Cross-Service Dependencies:** Called asynchronously by `claims-service` and `insurance-policy-service` when workflow status updates occur.
10. **Important Validations:** Non-null recipient `userId`, non-empty title and message strings.
11. **Important Exceptions:** `NotificationNotFoundException`, `UnauthorizedNotificationAccessException`.
12. **Logging/Monitoring/Actuator Support:** SLF4J logging for notification creation and mark-as-read events; Spring Boot Actuator enabled.
13. **Database Ownership:** Isolated `notificationdb` table `notifications`.
14. **Existing Tests:** `NotificationServiceApplicationTests.java`, `NotificationServiceTest.java`.
15. **Tests Added:** Executed 8 unit tests in `NotificationServiceTest`.
16. **Testing Limitations:** Push notifications / WebSocket streaming not implemented in test runner.
17. **Known Limitations:** In-memory H2 DB; real-time browser push requires WebSockets / SSE.
18. **Key Configuration Requirements:** `server.port=8087`, `spring.r2dbc.url=r2dbc:h2:mem:///notificationdb`.
