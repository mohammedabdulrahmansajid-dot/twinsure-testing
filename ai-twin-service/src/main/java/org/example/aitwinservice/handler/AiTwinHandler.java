package org.example.aitwinservice.handler;

// Handles Customer-owned AI Twin registration, details, configuration,
// and permissions. JWT identity and token data are forwarded so backend
// services can enforce ownership and active-policy configuration locks.

import org.example.aitwinservice.dto.request.CreateAiTwinRequestDTO;
import org.example.aitwinservice.dto.request.TwinPermissionRequestDTO;
import org.example.aitwinservice.dto.request.UpdateAiTwinRequestDTO;
import org.example.aitwinservice.exception.InvalidAiTwinConfigurationException;
import org.example.aitwinservice.filter.JwtFilter;
import org.example.aitwinservice.service.AiTwinService;
import org.example.aitwinservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AiTwinHandler {

    private final AiTwinService aiTwinService;
    private final RequestValidator requestValidator;

    public AiTwinHandler(
            AiTwinService aiTwinService,
            RequestValidator requestValidator) {

        this.aiTwinService =
                aiTwinService;

        this.requestValidator =
                requestValidator;
    }

    public Mono<ServerResponse> registerAiTwin(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                CreateAiTwinRequestDTO.class
                        )
                                .flatMap(request ->
                                        aiTwinService.registerAiTwin(
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

    public Mono<ServerResponse> getMyAiTwins(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        aiTwinService.getMyAiTwins(
                                                jwtDetails.customerId()
                                        ),
                                        Object.class
                                )
                );
    }

    public Mono<ServerResponse> getAiTwinDetails(
            ServerRequest serverRequest) {

        Long twinId =
                getLongPathVariable(
                        serverRequest,
                        "twinId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        aiTwinService
                                .getOwnedAiTwinDetails(
                                        twinId,
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

    public Mono<ServerResponse> getConfigurationLock(
            ServerRequest serverRequest) {

        Long twinId =
                getLongPathVariable(
                        serverRequest,
                        "twinId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        aiTwinService
                                .getOwnedConfigurationLock(
                                        twinId,
                                        jwtDetails.customerId(),
                                        jwtDetails.token()
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

    public Mono<ServerResponse> updateAiTwin(
            ServerRequest serverRequest) {

        Long twinId =
                getLongPathVariable(
                        serverRequest,
                        "twinId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                UpdateAiTwinRequestDTO.class
                        )
                                .flatMap(request ->
                                        aiTwinService.updateOwnedAiTwin(
                                                twinId,
                                                jwtDetails.customerId(),
                                                jwtDetails.token(),
                                                request
                                        )
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

    public Mono<ServerResponse> getPermissions(
            ServerRequest serverRequest) {

        Long twinId =
                getLongPathVariable(
                        serverRequest,
                        "twinId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        aiTwinService
                                                .getOwnedPermissions(
                                                        twinId,
                                                        jwtDetails.customerId()
                                                ),
                                        Object.class
                                )
                );
    }

    public Mono<ServerResponse> configurePermission(
            ServerRequest serverRequest) {

        Long twinId =
                getLongPathVariable(
                        serverRequest,
                        "twinId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                TwinPermissionRequestDTO.class
                        )
                                .flatMap(request ->
                                        aiTwinService.configurePermission(
                                                twinId,
                                                jwtDetails.customerId(),
                                                jwtDetails.token(),
                                                request
                                        )
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

    private Mono<JwtFilter.JwtUserDetails>
    getJwtDetails() {

        return ReactiveSecurityContextHolder
                .getContext()
                .map(
                        context ->
                                context.getAuthentication()
                )
                .filter(
                        Authentication::isAuthenticated
                )
                .map(
                        Authentication::getDetails
                )
                .cast(
                        JwtFilter.JwtUserDetails.class
                )
                .switchIfEmpty(
                        Mono.error(
                                new InvalidAiTwinConfigurationException(
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
                .bodyToMono(
                        requestType
                )
                .switchIfEmpty(
                        Mono.error(
                                new InvalidAiTwinConfigurationException(
                                        "Request body is required"
                                )
                        )
                )
                .flatMap(
                        requestValidator::validate
                );
    }

    private Long getLongPathVariable(
            ServerRequest serverRequest,
            String variableName) {

        String value =
                serverRequest.pathVariable(
                        variableName
                );

        try {

            return Long.valueOf(
                    value
            );

        } catch (NumberFormatException exception) {

            throw new InvalidAiTwinConfigurationException(
                    variableName
                            + " must be a valid number"
            );
        }
    }
    public Mono<ServerResponse> getInternalRiskProfile(
            ServerRequest serverRequest) {

        Long twinId =
                getLongPathVariable(
                        serverRequest,
                        "twinId"
                );

        return aiTwinService
                .getRiskProfile(
                        twinId
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
}