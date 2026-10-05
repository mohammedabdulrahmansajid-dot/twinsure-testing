package org.example.insurancepolicyservice.handler;

// Handles Customer, staff, and Admin requests for issued policies.
// This file connects policy routes to PolicyService and applies
// ownership information obtained from the authenticated JWT.

import org.example.insurancepolicyservice.dto.request.PolicyStatusRequestDTO;
import org.example.insurancepolicyservice.dto.response.PolicyResponseDTO;
import org.example.insurancepolicyservice.exception.InvalidRequestException;
import org.example.insurancepolicyservice.filter.JwtFilter;
import org.example.insurancepolicyservice.service.PolicyService;
import org.example.insurancepolicyservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class PolicyHandler {

    private final PolicyService policyService;
    private final RequestValidator requestValidator;

    public PolicyHandler(
            PolicyService policyService,
            RequestValidator requestValidator) {

        this.policyService = policyService;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> acceptApplication(
            ServerRequest serverRequest) {

        Long applicationId =
                getLongPathVariable(
                        serverRequest,
                        "applicationId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        policyService.acceptApplication(
                                applicationId,
                                jwtDetails.customerId(),
                                jwtDetails.token()
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

    public Mono<ServerResponse> getMyPolicies(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        policyService.getMyPolicies(
                                                jwtDetails.customerId()
                                        ),
                                        PolicyResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> getPolicyDetails(
            ServerRequest serverRequest) {

        Long policyId =
                getLongPathVariable(
                        serverRequest,
                        "policyId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        policyService.getPolicyDetails(
                                policyId,
                                jwtDetails.role(),
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

    public Mono<ServerResponse> getPolicyCoverage(
            ServerRequest serverRequest) {

        Long policyId =
                getLongPathVariable(
                        serverRequest,
                        "policyId"
                );

        return policyService
                .getPolicyCoverage(policyId)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getAllPolicies(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(
                        policyService.getAllPolicies(),
                        PolicyResponseDTO.class
                );
    }

    public Mono<ServerResponse> updatePolicyStatus(
            ServerRequest serverRequest) {

        Long policyId =
                getLongPathVariable(
                        serverRequest,
                        "policyId"
                );

        return readAndValidateBody(
                serverRequest,
                PolicyStatusRequestDTO.class
        )
                .flatMap(request ->
                        policyService.updatePolicyStatus(
                                policyId,
                                request
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