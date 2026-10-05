package org.example.aiactionservice.router;

// Defines functional routes for AI action simulation and review.
// Route groups separate Customer-owned operations from Admin and
// Claims Adjuster operations protected by Spring Security.

import org.example.aiactionservice.handler.AiActionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AiActionRouter {

    @Bean
    public RouterFunction<ServerResponse> aiActionRoutes(
            AiActionHandler handler) {

        return route()

                // Customer simulates an action
                .POST(
                        "/api/ai-actions/simulate",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::simulateAction
                )

                // Customer views owned action history
                .GET(
                        "/api/ai-actions/my",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getMyActions
                )

                // Admin views all actions
                .GET(
                        "/api/admin/ai-actions",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getAllActions
                )

                // Admin views complete action details
                .GET(
                        "/api/admin/ai-actions/{actionId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getActionDetailsForAdmin
                )

                // Claims staff view claim-eligible violations
                .GET(
                        "/api/claims/ai-actions/claim-eligible",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getClaimEligibleViolations
                )

                // Claims staff view one action and its violations
                .GET(
                        "/api/claims/ai-actions/{actionId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getActionForClaimReview
                )

                // Customer views one owned action
                .GET(
                        "/api/ai-actions/{actionId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getOwnedActionDetails
                )

                .build();
    }
}