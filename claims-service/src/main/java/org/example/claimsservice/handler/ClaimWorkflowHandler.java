package org.example.claimsservice.handler;

// Handles Admin claim assignment and Claims Adjuster workflow decisions.
// It reads authenticated JWT details, validates request bodies,
// and delegates status transitions to ClaimWorkflowService.

import org.example.claimsservice.dto.request.ApproveClaimRequestDTO;
import org.example.claimsservice.dto.request.AssignClaimRequestDTO;
import org.example.claimsservice.dto.request.ClaimReasonRequestDTO;
import org.example.claimsservice.dto.response.ClaimResponseDTO;
import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.filter.JwtFilter;
import org.example.claimsservice.service.ClaimWorkflowService;
import org.example.claimsservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ClaimWorkflowHandler {

    private final ClaimWorkflowService workflowService;
    private final RequestValidator requestValidator;

    public ClaimWorkflowHandler(
            ClaimWorkflowService workflowService,
            RequestValidator requestValidator) {

        this.workflowService = workflowService;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> assignClaim(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                AssignClaimRequestDTO.class
                        )
                                .flatMap(request ->
                                        workflowService.assignClaim(
                                                claimId,
                                                jwtDetails.userId(),
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

    public Mono<ServerResponse> getMyAssignedClaims(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        workflowService
                                                .getMyAssignedClaims(
                                                        jwtDetails.userId()
                                                ),
                                        ClaimResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> startReview(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                ClaimReasonRequestDTO.class
                        )
                                .flatMap(request ->
                                        workflowService.startReview(
                                                claimId,
                                                jwtDetails.userId(),
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

    public Mono<ServerResponse> requestInformation(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                ClaimReasonRequestDTO.class
                        )
                                .flatMap(request ->
                                        workflowService.requestInformation(
                                                claimId,
                                                jwtDetails.userId(),
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

    public Mono<ServerResponse> approveClaim(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                ApproveClaimRequestDTO.class
                        )
                                .flatMap(request ->
                                        workflowService.approveClaim(
                                                claimId,
                                                jwtDetails.userId(),
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

    public Mono<ServerResponse> partiallyApproveClaim(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                ApproveClaimRequestDTO.class
                        )
                                .flatMap(request ->
                                        workflowService.partiallyApproveClaim(
                                                claimId,
                                                jwtDetails.userId(),
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

    public Mono<ServerResponse> rejectClaim(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                ClaimReasonRequestDTO.class
                        )
                                .flatMap(request ->
                                        workflowService.rejectClaim(
                                                claimId,
                                                jwtDetails.userId(),
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

    public Mono<ServerResponse> closeClaim(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                ClaimReasonRequestDTO.class
                        )
                                .flatMap(request ->
                                        workflowService.closeClaim(
                                                claimId,
                                                jwtDetails.userId(),
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