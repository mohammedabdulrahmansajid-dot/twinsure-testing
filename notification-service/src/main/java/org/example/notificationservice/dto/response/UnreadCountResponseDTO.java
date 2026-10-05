package org.example.notificationservice.dto.response;

public record UnreadCountResponseDTO(

        Long recipientUserId,
        long unreadCount

) {
}