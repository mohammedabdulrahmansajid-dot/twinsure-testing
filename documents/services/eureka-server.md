# Eureka Server Documentation

1. **Purpose:** Central service registry enabling dynamic service discovery, load balancing, and health monitoring for all TwinSure microservices.
2. **Main Responsibilities:** Microservice registration, heartbeat monitoring, registry caching, service instance lookup.
3. **Technology and Persistence:** Java 17, Spring Boot 3.4.3, Spring Cloud Netflix Eureka Server. In-memory registry table.
4. **Important Packages/Components:** `org.example.eurekaserver.EurekaServerApplication`.
5. **Main Domain Entities:** InstanceInfo (service registry records).
6. **Main DTO Categories:** Eureka REST XML/JSON registration payloads.
7. **Main API Groups:** `/eureka/apps`, `/eureka/apps/{appId}`.
8. **Authentication and Authorization:** Internal discovery port accessible by microservices inside internal network perimeter.
9. **Cross-Service Dependencies:** Serves as discovery backbone for `api-gateway` and inter-service WebClients.
10. **Important Validations:** Heartbeat interval renewal within 90 seconds.
11. **Important Exceptions:** `ClientRegistrationException`, `InstanceNotFoundException`.
12. **Logging/Monitoring/Actuator Support:** Eureka Dashboard UI at `http://localhost:8761`.
13. **Database Ownership:** None (In-memory registry).
14. **Existing Tests:** `EurekaServerApplicationTests.java`.
15. **Tests Added:** Context load verification test.
16. **Testing Limitations:** Peer replication omitted in single-node dev mode.
17. **Known Limitations:** In-memory registry data lost on process shutdown.
18. **Key Configuration Requirements:** `server.port=8761`, `eureka.client.registerWithEureka=false`, `eureka.client.fetchRegistry=false`.
