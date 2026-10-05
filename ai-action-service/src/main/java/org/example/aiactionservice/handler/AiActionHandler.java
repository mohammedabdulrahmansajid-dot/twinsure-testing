package org.example.aiactionservice.handler;

// Handles Customer, Admin, and Claims Adjuster requests for AI actions.
// It reads authenticated JWT details, validates simulation requests,
// and delegates action operations to ActionEvaluationService.

import org.example.aiactionservice.dto.request.SimulateActionRequestDTO;
import org.example.aiactionservice.dto.response.ActionViolationResponseDTO;
import org.example.aiactionservice.dto.response.AiActionResponseDTO;
import org.example.aiactionservice.exception.InvalidRequestException;
import org.example.aiactionservice.filter.JwtFilter;
import org.example.aiactionservice.service.ActionEvaluationService;
import org.example.aiactionservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AiActionHandler {

    private final ActionEvaluationService actionService;
    private final RequestValidator requestValidator;

    public AiActionHandler(
            ActionEvaluationService actionService,
            RequestValidator requestValidator) {

        this.actionService = actionService;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> simulateAction(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                SimulateActionRequestDTO.class
                        )
                                .flatMap(request ->
                                        actionService.simulateAction(
                                                jwtDetails.userId(),
                                                jwtDetails.customerId(),
                                                jwtDetails.token(),
                                                request
                                        )
                                )
                )
                .flatMap(response ->
                        ServerResponse
                                .status(201)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getMyActions(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        actionService.getMyActions(
                                                jwtDetails.customerId()
                                        ),
                                        AiActionResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> getOwnedActionDetails(
            ServerRequest serverRequest) {

        Long actionId =
                getLongPathVariable(
                        serverRequest,
                        "actionId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        actionService.getOwnedActionDetails(
                                actionId,
                                jwtDetails.customerId()
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getAllActions(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(
                        actionService.getAllActions(),
                        AiActionResponseDTO.class
                );
    }

    public Mono<ServerResponse> getActionDetailsForAdmin(
            ServerRequest serverRequest) {

        Long actionId =
                getLongPathVariable(
                        serverRequest,
                        "actionId"
                );

        return actionService
                .getActionDetailsForAdmin(actionId)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getActionForClaimReview(
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

    public Mono<ServerResponse> getClaimEligibleViolations(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(
                        actionService.getClaimEligibleViolations(),
                        ActionViolationResponseDTO.class
                );
    }

    private Mono<JwtFilter.JwtUserDetails> getJwtDetails() {

        return ReactiveSecurityContextHolder
                .getContext()
                .map(context ->
                        context.getAuthentication()
                )
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getDetails)
                .cast(JwtFilter.JwtUserDetails.class)
                .switchIfEmpty(
                        Mono.error(
                                new InvalidRequestException(
                                        "Authenticated user details "
                                                + "are unavailable"
                                )
                        )
                );
    }

    private <T> Mono<T> readAndValidateBody(
            ServerRequest serverRequest,
            Class<T> requestType) {

        return serverRequest
                .bodyToMono(requestType)
                .switchIfEmpty(
                        Mono.error(
                                new InvalidRequestException(
                                        "Request body is required"
                                )
                        )
                )
                .flatMap(requestValidator::validate);
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