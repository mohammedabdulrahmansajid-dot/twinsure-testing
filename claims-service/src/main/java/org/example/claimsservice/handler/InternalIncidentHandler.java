package org.example.claimsservice.handler;

// Handles authenticated service-to-service incident retrieval.
// Other TwinSure services can use this focused endpoint without
// accessing Customer or Admin-facing incident routes.

import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.service.IncidentService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class InternalIncidentHandler {

    private final IncidentService incidentService;

    public InternalIncidentHandler(
            IncidentService incidentService) {

        this.incidentService = incidentService;
    }

    public Mono<ServerResponse> getInternalIncident(
            ServerRequest serverRequest) {

        Long incidentId =
                getLongPathVariable(
                        serverRequest,
                        "incidentId"
                );

        return incidentService
                .getInternalIncident(incidentId)
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