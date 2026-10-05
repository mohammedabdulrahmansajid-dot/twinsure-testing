package org.example.aiactionservice.dto.request;

// Carries an internal notification request to Notification Service.
// AI Action Service uses it to notify the Customer when an action
// contains one or more detected rule violations.

public record CreateNotificationRequestDTO(

        Long recipientUserId,
        String notificationType,
        String title,
        String message,
        String referenceType,
        Long referenceId,
        String sourceService,
        String priority

) {
}