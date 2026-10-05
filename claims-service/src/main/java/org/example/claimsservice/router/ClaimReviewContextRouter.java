package org.example.claimsservice.router;

// Defines the assigned Claims Adjuster's review-context endpoint.
// This focused route returns eligibility and maximum-payable data
// without mixing supporting-service failures into core claim details.

import org.example.claimsservice.handler.ClaimReviewContextHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ClaimReviewContextRouter {

    @Bean
    public RouterFunction<ServerResponse>
    claimReviewContextRoutes(
            ClaimReviewContextHandler handler) {

        return route()
                .GET(
                        "/api/claims-adjuster/claims/"
                                + "{claimId}/review-context",
                        accept(
                                MediaType.APPLICATION_JSON
                        ),
                        handler::getReviewContext
                )
                .build();
    }
}