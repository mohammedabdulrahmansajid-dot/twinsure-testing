package org.example.notificationservice.router;

// Defines functional routes for notification creation, retrieval, and read updates.
// User routes operate only on the authenticated recipient's notifications,
// while Admin can view all stored notifications.

import org.example.notificationservice.handler.NotificationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class NotificationRouter {

    @Bean
    public RouterFunction<ServerResponse> notificationRoutes(
            NotificationHandler handler) {

        return route()
                .POST(
                        "/internal/notifications",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::createNotification
                )
                .GET(
                        "/api/notifications/my/unread-count",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getUnreadCount
                )
                .GET(
                        "/api/notifications/my/unread",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getMyUnreadNotifications
                )
                .PUT(
                        "/api/notifications/my/read-all",
                        accept(MediaType.APPLICATION_JSON),
                        handler::markAllAsRead
                )
                .GET(
                        "/api/notifications/my",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getMyNotifications
                )
                .PUT(
                        "/api/notifications/{notificationId}/read",
                        accept(MediaType.APPLICATION_JSON),
                        handler::markAsRead
                )
                .GET(
                        "/api/admin/notifications",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getAllNotifications
                )
                .build();
    }
}
