package org.example.apigateway.config;

// Defines all public TwinSure routes and their destination microservices.
// The Gateway uses Eureka service names instead of fixed service ports.
// Internal service-to-service endpoints are intentionally not exposed here.

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator twinSureRoutes(
            RouteLocatorBuilder builder) {

        return builder.routes()

                .route(
                        "identity-service-route",
                        route -> route
                                .path(
                                        "/api/auth/**",
                                        "/api/admin/users/**"
                                )
                                .uri(
                                        "lb://IDENTITY-SERVICE"
                                )
                )

                .route(
                        "customer-service-route",
                        route -> route
                                .path(
                                        "/api/customers/**",
                                        "/api/admin/customers/**"
                                )
                                .uri(
                                        "lb://CUSTOMER-SERVICE"
                                )
                )

                .route(
                        "ai-twin-service-route",
                        route -> route
                                .path(
                                        "/api/ai-twins/**",
                                        "/api/admin/ai-twins/**"
                                )
                                .uri(
                                        "lb://AI-TWIN-SERVICE"
                                )
                )

                .route(
                        "insurance-product-route",
                        route -> route
                                .path(
                                        "/api/insurance-products/**",
                                        "/api/admin/insurance-products/**"
                                )
                                .uri(
                                        "lb://INSURANCE-POLICY-SERVICE"
                                )
                )

                .route(
                        "policy-application-route",
                        route -> route
                                .path(
                                        "/api/policy-applications/**",
                                        "/api/admin/policy-applications/**"
                                )
                                .uri(
                                        "lb://INSURANCE-POLICY-SERVICE"
                                )
                )

                .route(
                        "issued-policy-route",
                        route -> route
                                .path(
                                        "/api/policies/**",
                                        "/api/admin/policies/**"
                                )
                                .uri(
                                        "lb://INSURANCE-POLICY-SERVICE"
                                )
                )

                .route(
                        "ai-action-route",
                        route -> route
                                .path(
                                        "/api/ai-actions/**",
                                        "/api/admin/ai-actions/**",
                                        "/api/claims/ai-actions/**"
                                )
                                .uri(
                                        "lb://AI-ACTION-SERVICE"
                                )
                )

                .route(
                        "incident-route",
                        route -> route
                                .path(
                                        "/api/incidents/**",
                                        "/api/admin/incidents/**"
                                )
                                .uri(
                                        "lb://CLAIMS-SERVICE"
                                )
                )

                .route(
                        "claims-route",
                        route -> route
                                .path(
                                        "/api/claims/**",
                                        "/api/admin/claims/**",
                                        "/api/claims-adjuster/**"
                                )
                                .uri(
                                        "lb://CLAIMS-SERVICE"
                                )
                )

                .route(
                        "notification-route",
                        route -> route
                                .path(
                                        "/api/notifications/**",
                                        "/api/admin/notifications/**"
                                )
                                .uri(
                                        "lb://NOTIFICATION-SERVICE"
                                )
                )

                .build();
    }
}