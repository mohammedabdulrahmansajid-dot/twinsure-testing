package org.example.claimsservice.router;

// Defines the internal claim endpoint used by other TwinSure services.
// The route is authenticated and is not intended for normal frontend use.

import org.example.claimsservice.handler.InternalClaimHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class InternalClaimRouter {

    @Bean
    public RouterFunction<ServerResponse> internalClaimRoutes(
            InternalClaimHandler handler) {

        return route()

                .GET(
                        "/internal/claims/{claimId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getInternalClaim
                )

                .build();
    }
}