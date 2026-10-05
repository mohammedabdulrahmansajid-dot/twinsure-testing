
// Represents one notification returned by Notification Service.
// The model supports Customer and staff notification histories.

export type NotificationType =
  | 'POLICY_APPLICATION_SUBMITTED'
  | 'POLICY_APPLICATION_APPROVED'
  | 'POLICY_APPLICATION_REJECTED'
  | 'POLICY_CHANGES_REQUIRED'
  | 'POLICY_ISSUED'
  | 'POLICY_CANCELLED'
  | 'AI_ACTION_VIOLATION_DETECTED'
  | 'INCIDENT_REPORTED'
  | 'CLAIM_SUBMITTED'
  | 'CLAIM_ASSIGNED'
  | 'CLAIM_INFORMATION_REQUIRED'
  | 'CLAIM_APPROVED'
  | 'CLAIM_PARTIALLY_APPROVED'
  | 'CLAIM_REJECTED'
  | 'CLAIM_CLOSED'
  | 'GENERAL';

export type NotificationPriority =
  | 'LOW'
  | 'NORMAL'
  | 'HIGH'
  | 'URGENT';

export interface Notification {
  notificationId: number;
  recipientUserId: number;
  notificationType: NotificationType;
  title: string;
  message: string;
  referenceType: string | null;
  referenceId: number | null;
  sourceService: string;
  priority: NotificationPriority;
  readStatus: boolean;
  createdAt: string;
  readAt: string | null;
}
