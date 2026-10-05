package org.example.aiactionservice.router;

// Defines internal AI action routes used by Claims Service.
// These routes provide evidence contracts and are not intended
// as normal Customer-facing frontend endpoints.

import org.example.aiactionservice.handler.InternalAiActionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class InternalAiActionRouter {

    @Bean
    public RouterFunction<ServerResponse> internalAiActionRoutes(
            InternalAiActionHandler handler) {

        return route()

                .GET(
                        "/internal/ai-actions/{actionId}/claim-evidence",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getActionForClaim
                )

                .build();
    }
}