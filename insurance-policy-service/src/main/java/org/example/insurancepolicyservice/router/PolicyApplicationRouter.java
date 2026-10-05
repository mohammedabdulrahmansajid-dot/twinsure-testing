package org.example.insurancepolicyservice.router;

// Defines Customer and Underwriter policy-application routes.
// Specific workflow routes are declared before the general details route
// so path variables cannot capture operation names incorrectly.

import org.example.insurancepolicyservice.handler.PolicyApplicationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PolicyApplicationRouter {

    @Bean
    public RouterFunction<ServerResponse> policyApplicationRoutes(
            PolicyApplicationHandler handler) {

        return route()

                .POST(
                        "/api/policy-applications",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::createApplication
                )

                .GET(
                        "/api/policy-applications/my",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getMyApplications
                )

                .GET(
                        "/api/policy-applications/pending",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getPendingApplications
                )

                .GET(
                        "/api/policy-applications/reviewed-by-me",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getReviewedByMe
                )

                .POST(
                        "/api/policy-applications/{applicationId}/assessment",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::assessApplication
                )

                .POST(
                        "/api/policy-applications/{applicationId}/request-changes",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::requestChanges
                )

                .POST(
                        "/api/policy-applications/{applicationId}/approve",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::approveApplication
                )

                .POST(
                        "/api/policy-applications/{applicationId}/reject",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::rejectApplication
                )

                // No request body is required for Customer resubmission.
                .POST(
                        "/api/policy-applications/{applicationId}/resubmit",
                        accept(MediaType.APPLICATION_JSON),
                        handler::resubmitApplication
                )


                // Customer declines an approved proposal.
                // No request body is required.

                .POST(
                        "/api/policy-applications/{applicationId}/decline",
                        accept(MediaType.APPLICATION_JSON),
                        handler::declineApplication
                )

                .GET(
                        "/api/admin/policy-applications",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getAllApplications
                )

                .GET(
                        "/api/policy-applications/{applicationId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getApplicationDetails
                )

                .build();
    }
}