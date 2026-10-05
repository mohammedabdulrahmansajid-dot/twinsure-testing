package org.example.claimsservice.handler;

// Handles the Claims Adjuster review-context request.
// It extracts authenticated user information and returns the
// eligibility and maximum-payable calculation for an assigned claim.

import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.filter.JwtFilter;
import org.example.claimsservice.service.ClaimReviewContextService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ClaimReviewContextHandler {

    private final ClaimReviewContextService
            reviewContextService;

    public ClaimReviewContextHandler(
            ClaimReviewContextService reviewContextService) {

        this.reviewContextService =
                reviewContextService;
    }

    public Mono<ServerResponse> getReviewContext(
            ServerRequest serverRequest) {

        Long claimId =
                getLongPathVariable(
                        serverRequest,
                        "claimId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        reviewContextService
                                .getReviewContext(
                                        claimId,
                                        jwtDetails.userId(),
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

    private Mono<JwtFilter.JwtUserDetails>
    getJwtDetails() {

        return ReactiveSecurityContextHolder
                .getContext()
                .map(context ->
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
                                new InvalidRequestException(
                                        "Authenticated user details "
                                                + "are unavailable"
                                )
                        )
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