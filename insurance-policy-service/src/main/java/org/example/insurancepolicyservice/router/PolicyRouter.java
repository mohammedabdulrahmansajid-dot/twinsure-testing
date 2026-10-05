package org.example.insurancepolicyservice.router;

// Defines routes for Customer acceptance and issued-policy operations.
// Specific routes such as coverage are declared before the general
// policy-details route to prevent incorrect route matching.

import org.example.insurancepolicyservice.handler.PolicyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PolicyRouter {

    @Bean
    public RouterFunction<ServerResponse> policyRoutes(
            PolicyHandler handler) {

        return route()

                .POST(
                        "/api/policy-applications/{applicationId}/accept",
                        accept(MediaType.APPLICATION_JSON),
                        handler::acceptApplication
                )

                .GET(
                        "/api/policies/my",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getMyPolicies
                )

                .GET(
                        "/api/policies/{policyId}/coverage",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getPolicyCoverage
                )

                .GET(
                        "/api/admin/policies",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getAllPolicies
                )

                .PUT(
                        "/api/admin/policies/{policyId}/status",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::updatePolicyStatus
                )

                .GET(
                        "/api/policies/{policyId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getPolicyDetails
                )

                .build();
    }
}