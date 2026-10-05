package org.example.claimsservice.service;

// Validates that a claim assignee is an existing active Claims Adjuster.
// Identity Service remains authoritative for user role and account status.

import org.example.claimsservice.dto.response.UserRoleValidationResponseDTO;
import org.example.claimsservice.exception.InvalidRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Service
public class AdjusterAssignmentValidator {

    private final WebClient.Builder webClientBuilder;

    public AdjusterAssignmentValidator(
            WebClient.Builder webClientBuilder) {

        this.webClientBuilder = webClientBuilder;
    }

    public Mono<Void> validate(
            Long adjusterId,
            String jwtToken) {

        validateAdjusterId(
                adjusterId
        );

        validateJwtToken(
                jwtToken
        );

        String url =
                "http://IDENTITY-SERVICE"
                        + "/internal/users/"
                        + adjusterId
                        + "/role-validation"
                        + "?requiredRole=CLAIMS_ADJUSTER";

        return webClientBuilder
                .build()
                .get()
                .uri(url)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .onStatus(
                        status ->
                                status.value() == 404,
                        response ->
                                Mono.error(
                                        new InvalidRequestException(
                                                "The selected Claims Adjuster "
                                                        + "user does not exist"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response ->
                                Mono.error(
                                        new InvalidRequestException(
                                                "The selected user could not "
                                                        + "be validated for "
                                                        + "claim assignment"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                Mono.error(
                                        new InvalidRequestException(
                                                "Identity Service is currently "
                                                        + "unavailable. Claim "
                                                        + "assignment cannot continue."
                                        )
                                )
                )
                .bodyToMono(
                        UserRoleValidationResponseDTO.class
                )
                .switchIfEmpty(
                        Mono.error(
                                new InvalidRequestException(
                                        "Identity Service returned an empty "
                                                + "user-validation response"
                                )
                        )
                )
                .flatMap(validation -> {

                    boolean validActiveAdjuster =
                            validation.valid()
                                    && adjusterId.equals(
                                    validation.userId()
                            )
                                    && "CLAIMS_ADJUSTER".equals(
                                    validation.role()
                            )
                                    && "ACTIVE".equals(
                                    validation.status()
                            );

                    if (!validActiveAdjuster) {

                        return Mono.<Void>error(
                                new InvalidRequestException(
                                        "The selected user is not an "
                                                + "active Claims Adjuster"
                                )
                        );
                    }

                    return Mono.<Void>empty();
                })
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new InvalidRequestException(
                                        "Identity Service is currently "
                                                + "unavailable. Claim "
                                                + "assignment cannot continue."
                                )
                );
    }

    private void validateAdjusterId(
            Long adjusterId) {

        if (adjusterId == null
                || adjusterId <= 0) {

            throw new InvalidRequestException(
                    "Claims Adjuster user ID "
                            + "must be a positive number"
            );
        }
    }

    private void validateJwtToken(
            String jwtToken) {

        if (jwtToken == null
                || jwtToken.isBlank()) {

            throw new InvalidRequestException(
                    "Authentication token is unavailable"
            );
        }
    }
}