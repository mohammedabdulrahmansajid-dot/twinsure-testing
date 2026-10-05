// Represents the result of marking all owned notifications as read.

export interface MarkAllReadResponse {
  recipientUserId: number;
  updatedCount: number;
  message: string;
}