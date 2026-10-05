package org.example.notificationservice.dto.response;

// Summarizes the result of marking all owned notifications as read.
// It tells the user how many unread notification records were updated.

public record MarkAllReadResponseDTO(

        Long recipientUserId,
        long updatedCount,
        String message

) {
}