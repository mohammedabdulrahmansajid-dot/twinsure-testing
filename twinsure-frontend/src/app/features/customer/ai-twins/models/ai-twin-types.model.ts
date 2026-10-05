// Defines the exact AI Twin enum values supported by the backend.
// Union types provide autocomplete and reject invalid configuration values.

export type AutonomyLevel =
  | 'ASSISTIVE'
  | 'SUPERVISED'
  | 'AUTONOMOUS';

export type AiTwinStatus =
  | 'ACTIVE'
  | 'SUSPENDED'
  | 'RETIRED';

export type ActionType =
  | 'TRAVEL_BOOKING'
  | 'ONLINE_PURCHASE'
  | 'SUBSCRIPTION_MANAGEMENT';

export type PermissionLevel =
  | 'PROHIBITED'
  | 'APPROVAL_REQUIRED'
  | 'ALLOWED';