package org.example.notificationservice.handler;

// Handles user, Admin, and internal requests for in-app notifications.
// It extracts authenticated user details, validates internal requests,
// and delegates notification operations to NotificationService.

import org.example.notificationservice.dto.request.CreateNotificationRequestDTO;
import org.example.notificationservice.dto.response.NotificationResponseDTO;
import org.example.notificationservice.exception.InvalidRequestException;
import org.example.notificationservice.filter.JwtFilter;
import org.example.notificationservice.service.NotificationService;
import org.example.notificationservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class NotificationHandler {

    private final NotificationService notificationService;
    private final RequestValidator requestValidator;

    public NotificationHandler(
            NotificationService notificationService,
            RequestValidator requestValidator) {

        this.notificationService = notificationService;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> createNotification(
            ServerRequest serverRequest) {

        return readAndValidateBody(
                serverRequest,
                CreateNotificationRequestDTO.class
        )
                .flatMap(notificationService::createNotification)
                .flatMap(response ->
                        ServerResponse
                                .status(201)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getMyNotifications(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(
                                        notificationService.getMyNotifications(
                                                jwtDetails.userId()
                                        ),
                                        NotificationResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> getMyUnreadNotifications(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(
                                        notificationService
                                                .getMyUnreadNotifications(
                                                        jwtDetails.userId()
                                                ),
                                        NotificationResponseDTO.class
                                )
                );
    }

    public Mono<ServerResponse> getUnreadCount(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        notificationService.getUnreadCount(
                                jwtDetails.userId()
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> markAsRead(
            ServerRequest serverRequest) {

        Long notificationId =
                getLongPathVariable(
                        serverRequest,
                        "notificationId"
                );

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        notificationService.markAsRead(
                                notificationId,
                                jwtDetails.userId()
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> markAllAsRead(
            ServerRequest serverRequest) {

        return getJwtDetails()
                .flatMap(jwtDetails ->
                        notificationService.markAllAsRead(
                                jwtDetails.userId()
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getAllNotifications(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        notificationService.getAllNotifications(),
                        NotificationResponseDTO.class
                );
    }

    private Mono<JwtFilter.JwtUserDetails> getJwtDetails() {

        return ReactiveSecurityContextHolder
                .getContext()
                .map(context -> context.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getDetails)
                .cast(JwtFilter.JwtUserDetails.class)
                .switchIfEmpty(
                        Mono.error(
                                new InvalidRequestException(
                                        "Authenticated user details are unavailable"
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
                serverRequest.pathVariable(variableName);

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new InvalidRequestException(
                    variableName + " must be a valid number"
            );
        }
    }
}
