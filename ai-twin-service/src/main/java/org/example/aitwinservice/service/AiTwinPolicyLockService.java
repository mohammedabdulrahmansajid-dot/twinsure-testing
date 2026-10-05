package org.example.aitwinservice.service;

// Checks whether active insurance protects an AI Twin configuration.
// Risk-sensitive changes are blocked while an active policy exists,
// preventing post-underwriting configuration manipulation.

import org.example.aitwinservice.dto.response.ActivePolicyResponseDTO;
import org.example.aitwinservice.exception.CustomerServiceUnavailableException;
import org.example.aitwinservice.exception.InvalidAiTwinConfigurationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Service
public class AiTwinPolicyLockService {

    private final WebClient.Builder webClientBuilder;

    public AiTwinPolicyLockService(
            WebClient.Builder webClientBuilder) {

        this.webClientBuilder =
                webClientBuilder;
    }

    public Mono<ActivePolicyResponseDTO> getPolicyLock(
            Long twinId,
            String jwtToken) {

        validateTwinId(
                twinId
        );

        validateJwtToken(
                jwtToken
        );

        return webClientBuilder
                .build()
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .scheme("http")
                                .host(
                                        "INSURANCE-POLICY-SERVICE"
                                )
                                .path(
                                        "/internal/policies/active"
                                )
                                .queryParam(
                                        "twinId",
                                        twinId
                                )
                                .build()
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response ->
                                Mono.error(
                                        new InvalidAiTwinConfigurationException(
                                                "Active insurance policy "
                                                        + "validation failed"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                Mono.error(
                                        new CustomerServiceUnavailableException(
                                                "Insurance Policy Service is "
                                                        + "currently unavailable"
                                        )
                                )
                )
                .bodyToMono(
                        ActivePolicyResponseDTO.class
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new CustomerServiceUnavailableException(
                                        "Insurance Policy Service is "
                                                + "currently unavailable"
                                )
                );
    }

    public Mono<Void> ensureConfigurationUnlocked(
            Long twinId,
            String jwtToken) {

        return getPolicyLock(
                twinId,
                jwtToken
        )
                .flatMap(activePolicy -> {

                    if (activePolicy.active()
                            && activePolicy.policyId()
                            != null) {

                        return Mono.error(
                                new InvalidAiTwinConfigurationException(
                                        "Risk-sensitive AI Twin "
                                                + "configuration cannot be "
                                                + "changed while active policy "
                                                + activePolicy.policyId()
                                                + " covers this AI Twin"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    private void validateTwinId(
            Long twinId) {

        if (twinId == null
                || twinId <= 0) {

            throw new InvalidAiTwinConfigurationException(
                    "AI Twin ID must be a positive number"
            );
        }
    }

    private void validateJwtToken(
            String jwtToken) {

        if (jwtToken == null
                || jwtToken.isBlank()) {

            throw new InvalidAiTwinConfigurationException(
                    "Authentication token is unavailable"
            );
        }
    }
}