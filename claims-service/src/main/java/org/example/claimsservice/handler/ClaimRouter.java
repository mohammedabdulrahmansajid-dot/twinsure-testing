package org.example.claimsservice.router;

// Defines functional routes for claim creation, ownership views, and evidence.
// Specific routes such as /my and /documents are declared before
// the general /{claimId} details route.

import org.example.claimsservice.handler.ClaimHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ClaimRouter {

    @Bean
    public RouterFunction<ServerResponse> claimRoutes(
            ClaimHandler handler) {

        return route()

                .POST(
                        "/api/claims",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::createClaim
                )

                .GET(
                        "/api/claims/my",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getMyClaims
                )

                .POST(
                        "/api/claims/{claimId}/documents",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::addDocument
                )

                .GET(
                        "/api/claims/{claimId}/documents",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getClaimDocuments
                )

                .GET(
                        "/api/admin/claims",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getAllClaims
                )

                .GET(
                        "/api/claims/{claimId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getClaimDetails
                )

                .build();
    }
}