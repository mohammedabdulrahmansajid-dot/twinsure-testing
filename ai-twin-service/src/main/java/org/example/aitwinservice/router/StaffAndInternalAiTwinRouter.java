package org.example.aitwinservice.router;

import org.example.aitwinservice.handler.AdminAiTwinHandler;
import org.example.aitwinservice.handler.AiTwinReviewHandler;
import org.example.aitwinservice.handler.InternalAiTwinHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class StaffAndInternalAiTwinRouter {

    @Bean
    public RouterFunction<ServerResponse>
    staffAndInternalAiTwinRoutes(
            AiTwinReviewHandler reviewHandler,
            AdminAiTwinHandler adminHandler,
            InternalAiTwinHandler internalHandler) {

        return route()

                .GET(
                        "/api/ai-twins/{twinId}/risk-profile",
                        accept(MediaType.APPLICATION_JSON),
                        reviewHandler::getRiskProfile
                )

                .GET(
                        "/api/ai-twins/{twinId}/claim-summary",
                        accept(MediaType.APPLICATION_JSON),
                        reviewHandler::getClaimSummary
                )

                .GET(
                        "/api/admin/ai-twins",
                        accept(MediaType.APPLICATION_JSON),
                        adminHandler::getAllAiTwins
                )

                .GET(
                        "/api/admin/ai-twins/{twinId}",
                        accept(MediaType.APPLICATION_JSON),
                        adminHandler::getAiTwinById
                )

                .PUT(
                        "/api/admin/ai-twins/{twinId}/status",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        adminHandler::updateAiTwinStatus
                )

                .GET(
                        "/internal/ai-twins/{twinId}/policy-validation",
                        accept(MediaType.APPLICATION_JSON),
                        internalHandler::validateForPolicy
                )

                .GET(
                        "/internal/ai-twins/{twinId}/claim-validation",
                        accept(MediaType.APPLICATION_JSON),
                        internalHandler::validateForClaim
                )

                .GET(
                        "/internal/ai-twins/{twinId}/action-rules",
                        accept(MediaType.APPLICATION_JSON),
                        internalHandler::getActionRules
                )

                .build();
    }
}
