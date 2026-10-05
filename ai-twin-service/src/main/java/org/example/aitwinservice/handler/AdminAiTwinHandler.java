package org.example.aitwinservice.handler;

import org.example.aitwinservice.dto.request.AiTwinStatusRequestDTO;
import org.example.aitwinservice.exception.InvalidAiTwinConfigurationException;
import org.example.aitwinservice.service.AiTwinService;
import org.example.aitwinservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AdminAiTwinHandler {

    private final AiTwinService aiTwinService;
    private final RequestValidator requestValidator;

    public AdminAiTwinHandler(
            AiTwinService aiTwinService,
            RequestValidator requestValidator) {

        this.aiTwinService = aiTwinService;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> getAllAiTwins(
            ServerRequest request) {

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        aiTwinService.getAllAiTwins(),
                        Object.class
                );
    }

    public Mono<ServerResponse> getAiTwinById(
            ServerRequest request) {

        Long twinId = getLongPathVariable(
                request,
                "twinId"
        );

        return aiTwinService
                .getAiTwinDetailsForAdmin(twinId)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> updateAiTwinStatus(
            ServerRequest request) {

        Long twinId = getLongPathVariable(
                request,
                "twinId"
        );

        return request
                .bodyToMono(
                        AiTwinStatusRequestDTO.class
                )
                .switchIfEmpty(
                        Mono.error(
                                new InvalidAiTwinConfigurationException(
                                        "Request body is required"
                                )
                        )
                )
                .flatMap(requestValidator::validate)
                .flatMap(statusRequest ->
                        aiTwinService.updateAiTwinStatus(
                                twinId,
                                statusRequest
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