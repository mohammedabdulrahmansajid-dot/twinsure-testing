package org.example.claimsservice.router;

// Defines routes for Admin assignment and Claims Adjuster decisions.
// Each route is protected by the matching role rule in SecurityConfig
// and delegates its work to ClaimWorkflowHandler.

import org.example.claimsservice.handler.ClaimWorkflowHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ClaimWorkflowRouter {

    @Bean
    public RouterFunction<ServerResponse> claimWorkflowRoutes(
            ClaimWorkflowHandler handler) {

        return route()

                .PUT(
                        "/api/admin/claims/{claimId}/assign",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::assignClaim
                )

                .GET(
                        "/api/claims-adjuster/claims/my",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getMyAssignedClaims
                )

                .PUT(
                        "/api/claims-adjuster/claims/{claimId}/start-review",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::startReview
                )

                .PUT(
                        "/api/claims-adjuster/claims/{claimId}/request-information",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::requestInformation
                )

                .PUT(
                        "/api/claims-adjuster/claims/{claimId}/approve",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::approveClaim
                )

                .PUT(
                        "/api/claims-adjuster/claims/{claimId}/partial-approve",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::partiallyApproveClaim
                )

                .PUT(
                        "/api/claims-adjuster/claims/{claimId}/reject",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::rejectClaim
                )

                .PUT(
                        "/api/claims-adjuster/claims/{claimId}/close",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::closeClaim
                )

                .build();
    }
}