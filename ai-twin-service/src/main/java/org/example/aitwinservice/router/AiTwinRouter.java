package org.example.aitwinservice.router;

// Defines Customer-owned AI Twin routes, including configuration-lock status.
// The lock route is declared before the general Twin details route
// so active-policy protection can be shown safely in the frontend.

import org.example.aitwinservice.handler.AiTwinHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AiTwinRouter {

    @Bean
    public RouterFunction<ServerResponse>
    customerAiTwinRoutes(
            AiTwinHandler handler) {

        return route()

                .POST(
                        "/api/ai-twins",
                        accept(
                                MediaType.APPLICATION_JSON
                        )
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::registerAiTwin
                )

                .GET(
                        "/api/ai-twins/my",
                        accept(
                                MediaType.APPLICATION_JSON
                        ),
                        handler::getMyAiTwins
                )

                .GET(
                        "/api/ai-twins/{twinId}/configuration-lock",
                        accept(
                                MediaType.APPLICATION_JSON
                        ),
                        handler::getConfigurationLock
                )

                .GET(
                        "/api/ai-twins/{twinId}/permissions",
                        accept(
                                MediaType.APPLICATION_JSON
                        ),
                        handler::getPermissions
                )

                .PUT(
                        "/api/ai-twins/{twinId}/permissions",
                        accept(
                                MediaType.APPLICATION_JSON
                        )
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::configurePermission
                )
                .GET(
                        "/internal/ai-twins/{twinId}/risk-profile",
                        accept(
                                MediaType.APPLICATION_JSON
                        ),
                        handler::getInternalRiskProfile
                )
                .GET(
                        "/api/ai-twins/{twinId}",
                        accept(
                                MediaType.APPLICATION_JSON
                        ),
                        handler::getAiTwinDetails
                )

                .PUT(
                        "/api/ai-twins/{twinId}",
                        accept(
                                MediaType.APPLICATION_JSON
                        )
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::updateAiTwin
                )

                .build();
    }
}