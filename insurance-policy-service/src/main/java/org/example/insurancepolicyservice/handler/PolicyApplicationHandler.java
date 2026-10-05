package org.example.insurancepolicyservice.handler;

// Handles Customer and Underwriter requests for policy applications.
// It reads authenticated JWT details, validates request DTOs,
// and delegates application workflow operations to the service layer.

import org.example.insurancepolicyservice.dto.request.CreatePolicyApplicationRequestDTO;
import org.example.insurancepolicyservice.dto.request.PolicyApprovalRequestDTO;
import org.example.insurancepolicyservice.dto.request.PolicyChangesRequestDTO;
import org.example.insurancepolicyservice.dto.request.PolicyRejectionRequestDTO;
import org.example.insurancepolicyservice.dto.request.RiskAssessmentRequestDTO;
import org.example.insurancepolicyservice.dto.response.PolicyApplicationResponseDTO;
import org.example.insurancepolicyservice.exception.InvalidRequestException;
import org.example.insurancepolicyservice.filter.JwtFilter;
import org.example.insurancepolicyservice.service.PolicyApplicationService;
import org.example.insurancepolicyservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class PolicyApplicationHandler {

    private final PolicyApplicationService applicationService;
    private final RequestValidator requestValidator;

    public PolicyApplicationHandler(
            PolicyApplicationService applicationService,
            RequestValidator requestValidator) {

        this.applicationService = applicationService;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> createApplication(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                CreatePolicyApplicationRequestDTO.class
                        )
                                .flatMap(request ->
                                        applicationService.createApplication(
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

    public Mono<ServerResponse> getMyApplications(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        applicationService
                                                .getMyApplications(
                                                        jwtDetails.customerId()
                                                ),
                                        PolicyApplicationResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> getPendingApplications(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(
                        applicationService
                                .getPendingApplications(),
                        PolicyApplicationResponseDTO.class
                );
    }

    public Mono<ServerResponse> getReviewedByMe(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(
                                        applicationService
                                                .getReviewedByUnderwriter(
                                                        jwtDetails.userId()
                                                ),
                                        PolicyApplicationResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> getApplicationDetails(
            ServerRequest serverRequest) {

        Long applicationId =
                getLongPathVariable(
                        serverRequest,
                        "applicationId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        applicationService.getApplicationDetails(
                                applicationId,
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

    public Mono<ServerResponse> assessApplication(
            ServerRequest serverRequest) {

        Long applicationId =
                getLongPathVariable(
                        serverRequest,
                        "applicationId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                RiskAssessmentRequestDTO.class
                        )
                                .flatMap(request ->
                                        applicationService.assessApplication(
                                                applicationId,
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

    public Mono<ServerResponse> requestChanges(
            ServerRequest serverRequest) {

        Long applicationId =
                getLongPathVariable(
                        serverRequest,
                        "applicationId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                PolicyChangesRequestDTO.class
                        )
                                .flatMap(request ->
                                        applicationService.requestChanges(
                                                applicationId,
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

    public Mono<ServerResponse> approveApplication(
            ServerRequest serverRequest) {

        Long applicationId =
                getLongPathVariable(
                        serverRequest,
                        "applicationId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                PolicyApprovalRequestDTO.class
                        )
                                .flatMap(request ->
                                        applicationService.approveApplication(
                                                applicationId,
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

    public Mono<ServerResponse> rejectApplication(
            ServerRequest serverRequest) {

        Long applicationId =
                getLongPathVariable(
                        serverRequest,
                        "applicationId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        readAndValidateBody(
                                serverRequest,
                                PolicyRejectionRequestDTO.class
                        )
                                .flatMap(request ->
                                        applicationService.rejectApplication(
                                                applicationId,
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

    public Mono<ServerResponse> resubmitApplication(
            ServerRequest serverRequest) {

        Long applicationId =
                getLongPathVariable(
                        serverRequest,
                        "applicationId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        applicationService.resubmitApplication(
                                applicationId,
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

    public Mono<ServerResponse> getAllApplications(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(
                        applicationService
                                .getAllApplications(),
                        PolicyApplicationResponseDTO.class
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

    public Mono<ServerResponse> declineApplication(
            ServerRequest serverRequest) {

        Long applicationId =
                getLongPathVariable(
                        serverRequest,
                        "applicationId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        applicationService.declineApplication(
                                applicationId,
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
}