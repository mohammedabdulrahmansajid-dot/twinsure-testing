package org.example.aitwinservice.handler;

import org.example.aitwinservice.exception.InvalidAiTwinConfigurationException;
import org.example.aitwinservice.service.AiTwinService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class InternalAiTwinHandler {

    private final AiTwinService aiTwinService;

    public InternalAiTwinHandler(
            AiTwinService aiTwinService) {

        this.aiTwinService = aiTwinService;
    }

    public Mono<ServerResponse> validateForPolicy(
            ServerRequest request) {

        Long twinId = getTwinId(request);
        Long customerId = getCustomerId(request);

        return aiTwinService
                .validateAiTwin(
                        twinId,
                        customerId
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

    public Mono<ServerResponse> validateForClaim(
            ServerRequest request) {

        Long twinId = getTwinId(request);
        Long customerId = getCustomerId(request);

        return aiTwinService
                .validateAiTwin(
                        twinId,
                        customerId
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

    public Mono<ServerResponse> getActionRules(
            ServerRequest request) {

        Long twinId = getTwinId(request);
        Long customerId = getCustomerId(request);

        return aiTwinService
                .getActionRules(
                        twinId,
                        customerId
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

    private Long getTwinId(
            ServerRequest request) {

        try {
            return Long.valueOf(
                    request.pathVariable("twinId")
            );
        } catch (NumberFormatException exception) {
            throw new InvalidAiTwinConfigurationException(
                    "twinId must be a valid number"
            );
        }
    }

    private Long getCustomerId(
            ServerRequest request) {

        String customerId =
                request.queryParam("customerId")
                        .orElseThrow(() ->
                                new InvalidAiTwinConfigurationException(
                                        "customerId query parameter is required"
                                )
                        );

        try {
            return Long.valueOf(customerId);
        } catch (NumberFormatException exception) {
            throw new InvalidAiTwinConfigurationException(
                    "customerId must be a valid number"
            );
        }
    }
}