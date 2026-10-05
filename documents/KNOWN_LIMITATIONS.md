# TwinSure Known Limitations

1. **In-Memory H2 Database Persistence:** Default configuration uses reactive H2 in-memory databases per service. Restarting a microservice resets its stored data.
2. **Dependencies in Customer & AI Twin Services:** `customer-service` and `ai-twin-service` pom.xml files omit `spring-boot-starter-test` in the baseline project.
3. **Node 24 / npm Exit Handler on Windows:** Running global `npm install` in background processes on Node v24 Windows environment triggers intermittent npm exit handler warnings.
