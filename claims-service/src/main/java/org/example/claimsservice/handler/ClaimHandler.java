package org.example.claimsservice.handler;

// Handles Customer, Claims Adjuster, and Admin requests for formal claims.
// It reads authenticated JWT details, validates request bodies,
// and delegates claim and document operations to ClaimService.

import org.example.claimsservice.dto.request.AddClaimDocumentRequestDTO;
import org.example.claimsservice.dto.request.CreateClaimRequestDTO;
import org.example.claimsservice.dto.response.ClaimDocumentResponseDTO;
import org.example.claimsservice.dto.response.ClaimResponseDTO;
import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.filter.JwtFilter;
import org.example.claimsservice.service.ClaimService;
import org.example.claimsservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ClaimHandler {

    private final ClaimService claimService;
    private final RequestValidator requestValidator;

    public ClaimHandler(
            ClaimService claimService,
            RequestValidator requestValidator) {

        this.claimService = claimService;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> createClaim(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                CreateClaimRequestDTO.class
                        )
                                .flatMap(request ->
                                        claimService.createClaim(
                                                jwtDetails.customerId(),
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

    public Mono<ServerResponse> getMyClaims(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        claimService.getMyClaims(
                                                jwtDetails.customerId()
                                        ),
                                        ClaimResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> getClaimDetails(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        claimService.getClaimDetails(
                                claimId,
                                jwtDetails.role(),
                                jwtDetails.customerId(),
                                jwtDetails.userId()
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

    public Mono<ServerResponse> addDocument(
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
                                AddClaimDocumentRequestDTO.class
                        )
                                .flatMap(request ->
                                        claimService.addDocument(
                                                claimId,
                                                jwtDetails.customerId(),
                                                jwtDetails.userId(),
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

    public Mono<ServerResponse> getClaimDocuments(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        claimService.getClaimDocuments(
                                                claimId,
                                                jwtDetails.role(),
                                                jwtDetails.customerId(),
                                                jwtDetails.userId()
                                        ),
                                        ClaimDocumentResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> getAllClaims(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(
                        claimService.getAllClaims(),
                        ClaimResponseDTO.class
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