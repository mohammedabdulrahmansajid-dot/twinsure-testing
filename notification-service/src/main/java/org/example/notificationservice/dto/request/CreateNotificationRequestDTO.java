package org.example.notificationservice.dto.request;

// Carries notification information submitted by another TwinSure service.
// The recipient is identified using the universal Identity Service user ID.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.notificationservice.enums.NotificationPriority;
import org.example.notificationservice.enums.NotificationType;

public record CreateNotificationRequestDTO(

        @NotNull(message = "Recipient user ID is required")
        @Positive(message = "Recipient user ID must be positive")
        Long recipientUserId,

        @NotNull(message = "Notification type is required")
        NotificationType notificationType,

        @NotBlank(message = "Notification title is required")
        @Size(
                max = 150,
                message = "Notification title must not exceed 150 characters"
        )
        String title,

        @NotBlank(message = "Notification message is required")
        @Size(
                max = 1000,
                message = "Notification message must not exceed 1000 characters"
        )
        String message,

        @Size(
                max = 50,
                message = "Reference type must not exceed 50 characters"
        )
        String referenceType,

        @Positive(message = "Reference ID must be positive")
        Long referenceId,

        @NotBlank(message = "Source service is required")
        @Size(
                max = 100,
                message = "Source service must not exceed 100 characters"
        )
        String sourceService,

        @NotNull(message = "Notification priority is required")
        NotificationPriority priority

) {
}