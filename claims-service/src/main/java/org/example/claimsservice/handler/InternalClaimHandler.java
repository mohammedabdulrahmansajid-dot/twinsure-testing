package org.example.claimsservice.handler;

// Handles authenticated service-to-service claim retrieval.
// This focused endpoint provides standard claim information
// without exposing Customer or Admin-facing routes.

import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.service.ClaimService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class InternalClaimHandler {

    private final ClaimService claimService;

    public InternalClaimHandler(
            ClaimService claimService) {

        this.claimService = claimService;
    }

    public Mono<ServerResponse> getInternalClaim(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return claimService
                .getInternalClaim(claimId)
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
}