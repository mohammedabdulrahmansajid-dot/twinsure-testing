package org.example.claimsservice.dto.request;

// Carries an internal notification request from Claims Service.
// It is used to notify Customers and Claims Adjusters about claim events.

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