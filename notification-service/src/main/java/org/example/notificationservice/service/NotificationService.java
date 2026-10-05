package org.example.notificationservice.service;

// Creates and manages in-app notifications for TwinSure users.
// It supports recipient-specific history, unread counts, and read updates.
// Internal microservices use this service to save new notifications.

import org.example.notificationservice.dto.request.CreateNotificationRequestDTO;
import org.example.notificationservice.dto.response.MarkAllReadResponseDTO;
import org.example.notificationservice.dto.response.NotificationResponseDTO;
import org.example.notificationservice.dto.response.UnreadCountResponseDTO;
import org.example.notificationservice.exception.InvalidRequestException;
import org.example.notificationservice.exception.NotificationNotFoundException;
import org.example.notificationservice.model.Notification;
import org.example.notificationservice.repo.NotificationRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepo notificationRepo;

    public NotificationService(
            NotificationRepo notificationRepo) {

        this.notificationRepo = notificationRepo;
    }

    public Mono<NotificationResponseDTO> createNotification(
            CreateNotificationRequestDTO request) {

        Notification notification = new Notification();

        notification.setRecipientUserId(
                request.recipientUserId()
        );

        notification.setNotificationType(
                request.notificationType()
        );

        notification.setTitle(
                request.title().trim()
        );

        notification.setMessage(
                request.message().trim()
        );

        notification.setReferenceType(
                normalizeOptionalText(
                        request.referenceType()
                )
        );

        notification.setReferenceId(
                request.referenceId()
        );

        notification.setSourceService(
                request.sourceService()
                        .trim()
                        .toUpperCase()
        );

        notification.setPriority(
                request.priority()
        );

        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadAt(null);

        return notificationRepo
                .save(notification)
                .map(this::convertToResponse)
                .doOnSuccess(response ->
                        LOGGER.info(
                                "Notification created: notificationId={}, "
                                        + "recipientUserId={}, type={}, sourceService={}",
                                response.notificationId(),
                                response.recipientUserId(),
                                response.notificationType(),
                                response.sourceService()
                        )
                );
    }

    public Flux<NotificationResponseDTO> getMyNotifications(
            Long userId) {

        validateUserId(userId);

        return notificationRepo
                .findAllByRecipientUserIdOrderByCreatedAtDesc(
                        userId
                )
                .map(this::convertToResponse);
    }

    public Flux<NotificationResponseDTO> getMyUnreadNotifications(
            Long userId) {

        validateUserId(userId);

        return notificationRepo
                .findAllByRecipientUserIdAndReadStatusFalseOrderByCreatedAtDesc(
                        userId
                )
                .map(this::convertToResponse);
    }

    public Mono<UnreadCountResponseDTO> getUnreadCount(
            Long userId) {

        validateUserId(userId);

        return notificationRepo
                .countByRecipientUserIdAndReadStatusFalse(
                        userId
                )
                .map(count ->
                        new UnreadCountResponseDTO(
                                userId,
                                count
                        )
                );
    }

    public Mono<NotificationResponseDTO> markAsRead(
            Long notificationId,
            Long userId) {

        validateUserId(userId);

        return notificationRepo
                .findByNotificationIdAndRecipientUserId(
                        notificationId,
                        userId
                )
                .switchIfEmpty(
                        Mono.error(
                                new NotificationNotFoundException(
                                        "Notification not found"
                                )
                        )
                )
                .flatMap(notification -> {

                    if (Boolean.TRUE.equals(
                            notification.getReadStatus()
                    )) {

                        return Mono.just(notification);
                    }

                    notification.setReadStatus(true);
                    notification.setReadAt(LocalDateTime.now());

                    return notificationRepo.save(notification);
                })
                .map(this::convertToResponse)
                .doOnSuccess(response ->
                        LOGGER.info(
                                "Notification marked as read: "
                                        + "notificationId={}, recipientUserId={}",
                                response.notificationId(),
                                response.recipientUserId()
                        )
                );
    }

    public Mono<MarkAllReadResponseDTO> markAllAsRead(
            Long userId) {

        validateUserId(userId);

        LocalDateTime readTime = LocalDateTime.now();

        return notificationRepo
                .findAllByRecipientUserIdAndReadStatusFalse(
                        userId
                )
                .map(notification -> {

                    notification.setReadStatus(true);
                    notification.setReadAt(readTime);

                    return notification;
                })
                .collectList()
                .flatMap(notifications -> {

                    if (notifications.isEmpty()) {

                        return Mono.just(
                                new MarkAllReadResponseDTO(
                                        userId,
                                        0,
                                        "No unread notifications were found"
                                )
                        );
                    }

                    long updatedCount = notifications.size();

                    return notificationRepo
                            .saveAll(notifications)
                            .then(
                                    Mono.just(
                                            new MarkAllReadResponseDTO(
                                                    userId,
                                                    updatedCount,
                                                    "All unread notifications were marked as read"
                                            )
                                    )
                            );
                })
                .doOnSuccess(response ->
                        LOGGER.info(
                                "Notifications marked as read: "
                                        + "recipientUserId={}, updatedCount={}",
                                response.recipientUserId(),
                                response.updatedCount()
                        )
                );
    }

    public Flux<NotificationResponseDTO> getAllNotifications() {

        return notificationRepo
                .findAllByOrderByCreatedAtDesc()
                .map(this::convertToResponse);
    }

    private NotificationResponseDTO convertToResponse(
            Notification notification) {

        return new NotificationResponseDTO(
                notification.getNotificationId(),
                notification.getRecipientUserId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getSourceService(),
                notification.getPriority(),
                notification.getReadStatus(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private String normalizeOptionalText(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value
                .trim()
                .toUpperCase();
    }

    private void validateUserId(
            Long userId) {

        if (userId == null) {

            throw new InvalidRequestException(
                    "Authenticated user ID is required"
            );
        }
    }
}
