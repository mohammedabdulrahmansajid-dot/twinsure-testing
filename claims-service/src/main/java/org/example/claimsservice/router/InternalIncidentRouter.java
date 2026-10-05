package org.example.claimsservice.router;

// Defines the internal incident endpoint used by other microservices.
// This route returns focused incident data and is protected by
// authenticated service-to-service security.

import org.example.claimsservice.handler.InternalIncidentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class InternalIncidentRouter {

    @Bean
    public RouterFunction<ServerResponse> internalIncidentRoutes(
            InternalIncidentHandler handler) {

        return route()

                .GET(
                        "/internal/incidents/{incidentId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getInternalIncident
                )

                .build();
    }
}