# TwinSure Setup & Execution Guide

## Prerequisites
- Java 17 or higher (Java 26 supported)
- Node.js 20+ and npm
- Maven (or use embedded `mvnw.cmd` wrapper in each service)

## Running Microservices
Start services in the following order:

1. **Eureka Server (Port 8761):**
   ```bash
   cd eureka-server
   .\mvnw.cmd spring-boot:run
   ```
2. **API Gateway (Port 8080):**
   ```bash
   cd api-gateway
   .\mvnw.cmd spring-boot:run
   ```
3. **Core Microservices (Ports 8081 - 8087):**
   Run `identity-service`, `customer-service`, `ai-twin-service`, `insurance-policy-service`, `ai-action-service`, `claims-service`, `notification-service`.

## Running Frontend
```bash
cd twinsure-frontend
npm install
npm start
```
Access application at `http://localhost:4200`.
