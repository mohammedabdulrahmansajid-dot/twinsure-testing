package org.example.claimsservice.handler;

// Handles Customer and Admin HTTP requests for insured incidents.
// It extracts JWT details, validates request bodies, and delegates
// incident reporting and retrieval operations to IncidentService.

import org.example.claimsservice.dto.request.ReportIncidentRequestDTO;
import org.example.claimsservice.dto.response.IncidentResponseDTO;
import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.filter.JwtFilter;
import org.example.claimsservice.service.IncidentService;
import org.example.claimsservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class IncidentHandler {

    private final IncidentService incidentService;
    private final RequestValidator requestValidator;

    public IncidentHandler(
            IncidentService incidentService,
            RequestValidator requestValidator) {

        this.incidentService = incidentService;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> reportIncident(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                ReportIncidentRequestDTO.class
                        )
                                .flatMap(request ->
                                        incidentService.reportIncident(
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

    public Mono<ServerResponse> getMyIncidents(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        incidentService.getMyIncidents(
                                                jwtDetails.customerId()
                                        ),
                                        IncidentResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> getOwnedIncidentDetails(
            ServerRequest serverRequest) {

        Long incidentId =
                getLongPathVariable(
                        serverRequest,
                        "incidentId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        incidentService.getOwnedIncidentDetails(
                                incidentId,
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

    public Mono<ServerResponse> getAllIncidents(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(
                        incidentService.getAllIncidents(),
                        IncidentResponseDTO.class
                );
    }

    public Mono<ServerResponse> getIncidentDetailsForAdmin(
            ServerRequest serverRequest) {

        Long incidentId =
                getLongPathVariable(
                        serverRequest,
                        "incidentId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        incidentService.getIncidentDetailsForAdmin(
                                incidentId,
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
                serverRequest.pathVariable(
                        variableName
                );

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