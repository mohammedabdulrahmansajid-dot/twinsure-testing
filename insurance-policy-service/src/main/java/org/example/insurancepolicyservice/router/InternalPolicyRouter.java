package org.example.insurancepolicyservice.router;

// Defines internal policy routes used by other TwinSure microservices.
// These routes are not normal frontend operations and provide focused
// policy validation and coverage contracts.

import org.example.insurancepolicyservice.handler.InternalPolicyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class InternalPolicyRouter {

    @Bean
    public RouterFunction<ServerResponse> internalPolicyRoutes(
            InternalPolicyHandler handler) {

        return route()

                .GET(
                        "/internal/policies/active",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getActivePolicyForTwin
                )

                .GET(
                        "/internal/policies/{policyId}/coverage",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getInternalPolicyCoverage
                )

                .GET(
                        "/internal/policies/{policyId}/incident-validation",
                        accept(MediaType.APPLICATION_JSON),
                        handler::validatePolicyForIncident
                )

                .build();
    }
}