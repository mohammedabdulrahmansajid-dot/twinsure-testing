package org.example.aitwinservice.handler;

import org.example.aitwinservice.service.AiTwinService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AiTwinReviewHandler {

    private final AiTwinService aiTwinService;

    public AiTwinReviewHandler(
            AiTwinService aiTwinService) {

        this.aiTwinService = aiTwinService;
    }

    public Mono<ServerResponse> getRiskProfile(
            ServerRequest request) {

        Long twinId = getLongPathVariable(
                request,
                "twinId"
        );

        return aiTwinService
                .getRiskProfile(twinId)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getClaimSummary(
            ServerRequest request) {

        Long twinId = getLongPathVariable(
                request,
                "twinId"
        );

        return aiTwinService
                .getClaimSummary(twinId)
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
            ServerRequest request,
            String variableName) {

        try {
            return Long.valueOf(
                    request.pathVariable(variableName)
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    variableName
                            + " must be a valid number"
            );
        }
    }
}