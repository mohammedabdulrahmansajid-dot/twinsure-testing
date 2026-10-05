package org.example.notificationservice.dto.response;

// Represents one stored notification in standard API responses.
// It includes the related business reference, priority,
// read status, and notification timestamps.

import org.example.notificationservice.enums.NotificationPriority;
import org.example.notificationservice.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDTO(

        Long notificationId,
        Long recipientUserId,
        NotificationType notificationType,
        String title,
        String message,
        String referenceType,
        Long referenceId,
        String sourceService,
        NotificationPriority priority,
        Boolean readStatus,
        LocalDateTime createdAt,
        LocalDateTime readAt

) {
}