package org.example.aiactionservice.handler;

// Provides AI action evidence to other TwinSure microservices.
// Claims Service uses this internal operation to retrieve the
// complete action record and every detected violation.

import org.example.aiactionservice.exception.InvalidRequestException;
import org.example.aiactionservice.service.ActionEvaluationService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class InternalAiActionHandler {

    private final ActionEvaluationService actionService;

    public InternalAiActionHandler(
            ActionEvaluationService actionService) {

        this.actionService = actionService;
    }

    public Mono<ServerResponse> getActionForClaim(
            ServerRequest serverRequest) {

        Long actionId =
                getLongPathVariable(
                        serverRequest,
                        "actionId"
                );

        return actionService
                .getActionForClaimReview(actionId)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    private Long getLongPathVariable(
            ServerRequest serverRequest,
            String variableName) {

        String value =
                serverRequest.pathVariable(variableName);

        try {

            return Long.valueOf(value);

        } catch (NumberFormatException exception) {

            throw new InvalidRequestException(
                    variableName
                            + " must be a valid number"
            );
        }
    }
}