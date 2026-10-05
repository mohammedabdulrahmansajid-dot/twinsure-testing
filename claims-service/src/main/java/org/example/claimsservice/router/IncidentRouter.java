package org.example.claimsservice.router;

// Defines functional routes for Customer and Admin incident operations.
// Specific routes such as /my are declared before the general
// /{incidentId} route to prevent incorrect path matching.

import org.example.claimsservice.handler.IncidentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class IncidentRouter {

    @Bean
    public RouterFunction<ServerResponse> incidentRoutes(
            IncidentHandler handler) {

        return route()

                .POST(
                        "/api/incidents",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::reportIncident
                )

                .GET(
                        "/api/incidents/my",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getMyIncidents
                )

                .GET(
                        "/api/admin/incidents",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getAllIncidents
                )

                .GET(
                        "/api/admin/incidents/{incidentId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getIncidentDetailsForAdmin
                )

                .GET(
                        "/api/incidents/{incidentId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getOwnedIncidentDetails
                )

                .build();
    }
}