package org.example.notificationservice.service;

// Tests notification creation, unread counts, and read-status updates.
// The repository is mocked so these tests run without Spring, H2,
// Eureka, Gateway, or any other TwinSure microservice.

import org.example.notificationservice.dto.request.CreateNotificationRequestDTO;
import org.example.notificationservice.dto.response.MarkAllReadResponseDTO;
import org.example.notificationservice.dto.response.NotificationResponseDTO;
import org.example.notificationservice.dto.response.UnreadCountResponseDTO;
import org.example.notificationservice.enums.NotificationPriority;
import org.example.notificationservice.enums.NotificationType;
import org.example.notificationservice.exception.NotificationNotFoundException;
import org.example.notificationservice.model.Notification;
import org.example.notificationservice.repo.NotificationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private NotificationRepo notificationRepo;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {

        notificationRepo =
                Mockito.mock(
                        NotificationRepo.class
                );

        notificationService =
                new NotificationService(
                        notificationRepo
                );
    }

    @Test
    void shouldCreateUnreadNotification() {

        CreateNotificationRequestDTO request =
                new CreateNotificationRequestDTO(
                        2L,
                        NotificationType.CLAIM_APPROVED,
                        " Claim approved ",
                        " Your claim has been approved. ",
                        " claim ",
                        1L,
                        " claims-service ",
                        NotificationPriority.HIGH
                );

        when(
                notificationRepo.save(
                        any(Notification.class)
                )
        )
                .thenAnswer(invocation -> {

                    Notification notification =
                            invocation.getArgument(0);

                    notification.setNotificationId(
                            10L
                    );

                    return Mono.just(notification);
                });

        StepVerifier.create(
                        notificationService.createNotification(
                                request
                        )
                )
                .assertNext(response -> {

                    assertEquals(
                            10L,
                            response.notificationId()
                    );

                    assertEquals(
                            2L,
                            response.recipientUserId()
                    );

                    assertEquals(
                            NotificationType.CLAIM_APPROVED,
                            response.notificationType()
                    );

                    assertEquals(
                            "Claim approved",
                            response.title()
                    );

                    assertEquals(
                            "Your claim has been approved.",
                            response.message()
                    );

                    assertEquals(
                            "CLAIM",
                            response.referenceType()
                    );

                    assertEquals(
                            "CLAIMS-SERVICE",
                            response.sourceService()
                    );

                    assertEquals(
                            NotificationPriority.HIGH,
                            response.priority()
                    );

                    assertEquals(
                            false,
                            response.readStatus()
                    );

                    assertNotNull(
                            response.createdAt()
                    );

                    assertEquals(
                            null,
                            response.readAt()
                    );
                })
                .verifyComplete();

        verify(notificationRepo)
                .save(
                        any(Notification.class)
                );
    }

    @Test
    void shouldReturnUnreadNotificationCount() {

        when(
                notificationRepo
                        .countByRecipientUserIdAndReadStatusFalse(
                                2L
                        )
        )
                .thenReturn(
                        Mono.just(3L)
                );

        StepVerifier.create(
                        notificationService.getUnreadCount(
                                2L
                        )
                )
                .assertNext(response -> {

                    assertEquals(
                            2L,
                            response.recipientUserId()
                    );

                    assertEquals(
                            3L,
                            response.unreadCount()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnUnreadNotificationsNewestFirst() {

        Notification first =
                createNotification(
                        2L,
                        1L,
                        false
                );

        Notification second =
                createNotification(
                        2L,
                        2L,
                        false
                );

        when(
                notificationRepo
                        .findAllByRecipientUserIdAndReadStatusFalseOrderByCreatedAtDesc(
                                2L
                        )
        )
                .thenReturn(
                        Flux.just(
                                second,
                                first
                        )
                );

        StepVerifier.create(
                        notificationService
                                .getMyUnreadNotifications(
                                        2L
                                )
                )
                .assertNext(response ->
                        assertEquals(
                                2L,
                                response.notificationId()
                        )
                )
                .assertNext(response ->
                        assertEquals(
                                1L,
                                response.notificationId()
                        )
                )
                .verifyComplete();
    }

    @Test
    void shouldMarkOwnedNotificationAsRead() {

        Notification notification =
                createNotification(
                        2L,
                        5L,
                        false
                );

        when(
                notificationRepo
                        .findByNotificationIdAndRecipientUserId(
                                5L,
                                2L
                        )
        )
                .thenReturn(
                        Mono.just(notification)
                );

        when(
                notificationRepo.save(
                        notification
                )
        )
                .thenReturn(
                        Mono.just(notification)
                );

        StepVerifier.create(
                        notificationService.markAsRead(
                                5L,
                                2L
                        )
                )
                .assertNext(response -> {

                    assertEquals(
                            5L,
                            response.notificationId()
                    );

                    assertTrue(
                            response.readStatus()
                    );

                    assertNotNull(
                            response.readAt()
                    );
                })
                .verifyComplete();

        verify(notificationRepo)
                .save(notification);
    }

    @Test
    void shouldReturnAlreadyReadNotificationWithoutSavingAgain() {

        Notification notification =
                createNotification(
                        2L,
                        6L,
                        true
                );

        when(
                notificationRepo
                        .findByNotificationIdAndRecipientUserId(
                                6L,
                                2L
                        )
        )
                .thenReturn(
                        Mono.just(notification)
                );

        StepVerifier.create(
                        notificationService.markAsRead(
                                6L,
                                2L
                        )
                )
                .assertNext(response -> {

                    assertEquals(
                            6L,
                            response.notificationId()
                    );

                    assertTrue(
                            response.readStatus()
                    );

                    assertNotNull(
                            response.readAt()
                    );
                })
                .verifyComplete();

        verify(
                notificationRepo,
                never()
        )
                .save(
                        any(Notification.class)
                );
    }

    @Test
    void shouldRejectUnknownOrUnownedNotification() {

        when(
                notificationRepo
                        .findByNotificationIdAndRecipientUserId(
                                99L,
                                2L
                        )
        )
                .thenReturn(
                        Mono.empty()
                );

        StepVerifier.create(
                        notificationService.markAsRead(
                                99L,
                                2L
                        )
                )
                .expectErrorMatches(exception ->
                        exception
                                instanceof NotificationNotFoundException
                                && exception.getMessage()
                                .equals(
                                        "Notification not found"
                                )
                )
                .verify();

        verify(
                notificationRepo,
                never()
        )
                .save(
                        any(Notification.class)
                );
    }

    @Test
    void shouldMarkAllUnreadNotificationsAsRead() {

        Notification first =
                createNotification(
                        2L,
                        7L,
                        false
                );

        Notification second =
                createNotification(
                        2L,
                        8L,
                        false
                );

        when(
                notificationRepo
                        .findAllByRecipientUserIdAndReadStatusFalse(
                                2L
                        )
        )
                .thenReturn(
                        Flux.just(
                                first,
                                second
                        )
                );

        when(
                notificationRepo.saveAll(
                        any(Iterable.class)
                )
        )
                .thenAnswer(invocation -> {

                    Iterable<Notification> notifications =
                            invocation.getArgument(0);

                    return Flux.fromIterable(
                            notifications
                    );
                });

        StepVerifier.create(
                        notificationService.markAllAsRead(
                                2L
                        )
                )
                .assertNext(response -> {

                    assertEquals(
                            2L,
                            response.recipientUserId()
                    );

                    assertEquals(
                            2L,
                            response.updatedCount()
                    );

                    assertEquals(
                            "All unread notifications were marked as read",
                            response.message()
                    );

                    assertTrue(
                            first.getReadStatus()
                    );

                    assertTrue(
                            second.getReadStatus()
                    );

                    assertNotNull(
                            first.getReadAt()
                    );

                    assertNotNull(
                            second.getReadAt()
                    );

                    assertSame(
                            first.getReadAt(),
                            second.getReadAt()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnZeroWhenNoUnreadNotificationsExist() {

        when(
                notificationRepo
                        .findAllByRecipientUserIdAndReadStatusFalse(
                                2L
                        )
        )
                .thenReturn(
                        Flux.empty()
                );

        StepVerifier.create(
                        notificationService.markAllAsRead(
                                2L
                        )
                )
                .assertNext(response -> {

                    assertEquals(
                            2L,
                            response.recipientUserId()
                    );

                    assertEquals(
                            0L,
                            response.updatedCount()
                    );

                    assertEquals(
                            "No unread notifications were found",
                            response.message()
                    );
                })
                .verifyComplete();

        verify(
                notificationRepo,
                never()
        )
                .saveAll(
                        any(Iterable.class)
                );
    }

    private Notification createNotification(
            Long recipientUserId,
            Long notificationId,
            boolean read) {

        LocalDateTime createdAt =
                LocalDateTime.now()
                        .minusMinutes(5);

        LocalDateTime readAt =
                read
                        ? LocalDateTime.now()
                        : null;

        return new Notification(
                notificationId,
                recipientUserId,
                NotificationType.CLAIM_ASSIGNED,
                "Claim assigned",
                "A claim has been assigned for review.",
                "CLAIM",
                1L,
                "CLAIMS-SERVICE",
                NotificationPriority.HIGH,
                read,
                createdAt,
                readAt
        );
    }
}