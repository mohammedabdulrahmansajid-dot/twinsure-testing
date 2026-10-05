package org.example.aitwinservice.router;

// Exposes AI Twin oversight and status management to Administrators.
// Route authorization remains enforced by the reactive security configuration.

import org.example.aitwinservice.handler.AdminAiTwinHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AdminAiTwinRouter {

    @Bean
    public RouterFunction<ServerResponse> adminAiTwinRoutes(
            AdminAiTwinHandler handler) {

        return route()

                .GET(
                        "/api/admin/ai-twins",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getAllAiTwins
                )

                .PUT(
                        "/api/admin/ai-twins/{twinId}/status",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::updateAiTwinStatus
                )

                .GET(
                        "/api/admin/ai-twins/{twinId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getAiTwinById
                )

                .build();
    }
}