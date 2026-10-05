// Matches the unread-count response returned by Notification Service.

export interface UnreadCountResponse {
  recipientUserId: number;
  unreadCount: number;
}