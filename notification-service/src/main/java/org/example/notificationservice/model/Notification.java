package org.example.notificationservice.model;

// Stores one in-app notification for a TwinSure user.
// The notification can reference a policy, claim, action, or application
// and tracks whether and when the recipient has read it.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.notificationservice.enums.NotificationPriority;
import org.example.notificationservice.enums.NotificationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("NOTIFICATIONS")
public class Notification {

    @Id
    @Column("NOTIFICATION_ID")
    private Long notificationId;

    @Column("RECIPIENT_USER_ID")
    private Long recipientUserId;

    @Column("NOTIFICATION_TYPE")
    private NotificationType notificationType;

    @Column("TITLE")
    private String title;

    @Column("MESSAGE")
    private String message;

    @Column("REFERENCE_TYPE")
    private String referenceType;

    @Column("REFERENCE_ID")
    private Long referenceId;

    @Column("SOURCE_SERVICE")
    private String sourceService;

    @Column("PRIORITY")
    private NotificationPriority priority;

    @Column("READ_STATUS")
    private Boolean readStatus;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("READ_AT")
    private LocalDateTime readAt;
}