package org.example.insurancepolicyservice.handler;

// Handles service-to-service policy requests from AI Action and Claims services.
// It provides active-policy information, coverage rules, and validation
// for the date on which an AI action occurred.

import org.example.insurancepolicyservice.exception.InvalidRequestException;
import org.example.insurancepolicyservice.service.PolicyService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class InternalPolicyHandler {

    private final PolicyService policyService;

    public InternalPolicyHandler(
            PolicyService policyService) {

        this.policyService = policyService;
    }


    public Mono<ServerResponse> getActivePolicyForTwin(
            ServerRequest serverRequest) {

        Long twinId =
                getRequiredLongQueryParameter(
                        serverRequest,
                        "twinId"
                );

        return policyService
                .getActivePolicyForTwin(twinId)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getInternalPolicyCoverage(
            ServerRequest serverRequest) {

        Long policyId =
                getLongPathVariable(
                        serverRequest,
                        "policyId"
                );

        return policyService
                .getInternalPolicyCoverage(policyId)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> validatePolicyForIncident(
            ServerRequest serverRequest) {

        Long policyId =
                getLongPathVariable(
                        serverRequest,
                        "policyId"
                );

        Long customerId =
                getRequiredLongQueryParameter(
                        serverRequest,
                        "customerId"
                );

        Long twinId =
                getRequiredLongQueryParameter(
                        serverRequest,
                        "twinId"
                );

        LocalDate actionDate =
                getRequiredDateQueryParameter(
                        serverRequest,
                        "actionDate"
                );

        return policyService
                .validatePolicyForIncident(
                        policyId,
                        customerId,
                        twinId,
                        actionDate
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

    private Long getRequiredLongQueryParameter(
            ServerRequest serverRequest,
            String parameterName) {

        String value =
                serverRequest.queryParam(parameterName)
                        .orElseThrow(() ->
                                new InvalidRequestException(
                                        parameterName
                                                + " query parameter is required"
                                )
                        );

        try {

            return Long.valueOf(value);

        } catch (NumberFormatException exception) {

            throw new InvalidRequestException(
                    parameterName
                            + " must be a valid number"
            );
        }
    }

    private LocalDate getRequiredDateQueryParameter(
            ServerRequest serverRequest,
            String parameterName) {

        String value =
                serverRequest.queryParam(parameterName)
                        .orElseThrow(() ->
                                new InvalidRequestException(
                                        parameterName
                                                + " query parameter is required"
                                )
                        );

        try {

            return LocalDate.parse(value);

        } catch (DateTimeParseException exception) {

            throw new InvalidRequestException(
                    parameterName
                            + " must use yyyy-MM-dd format"
            );
        }
    }
}