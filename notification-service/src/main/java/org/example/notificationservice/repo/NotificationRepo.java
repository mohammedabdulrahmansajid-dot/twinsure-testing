package org.example.notificationservice.repo;

// Provides reactive database operations for in-app notifications.
// It supports recipient-specific history, unread notifications,
// unread counts, ownership checks, and bulk read updates.

import org.example.notificationservice.model.Notification;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationRepo
        extends ReactiveCrudRepository<Notification, Long> {

    Flux<Notification> findAllByRecipientUserIdOrderByCreatedAtDesc(
            Long recipientUserId
    );

    Flux<Notification>
    findAllByRecipientUserIdAndReadStatusFalseOrderByCreatedAtDesc(
            Long recipientUserId
    );

    Mono<Long> countByRecipientUserIdAndReadStatusFalse(
            Long recipientUserId
    );

    Mono<Notification>
    findByNotificationIdAndRecipientUserId(
            Long notificationId,
            Long recipientUserId
    );

    Flux<Notification>
    findAllByRecipientUserIdAndReadStatusFalse(
            Long recipientUserId
    );

    Flux<Notification> findAllByOrderByCreatedAtDesc();
}
