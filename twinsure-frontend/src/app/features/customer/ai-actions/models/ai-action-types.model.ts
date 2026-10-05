// Defines the exact enum values supported by AI Action Service.
// Scenario values describe factual outcomes used to derive violations.

export type ActionType =
  | 'TRAVEL_BOOKING'
  | 'ONLINE_PURCHASE'
  | 'SUBSCRIPTION_MANAGEMENT';

export type ActionStatus =
  | 'COMPLIANT'
  | 'VIOLATION_DETECTED'
  | 'EVALUATION_FAILED';

export type ViolationSeverity =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'CRITICAL';

export type ViolationType =
  | 'WRONG_ACTION'
  | 'APPROVAL_MISSING'
  | 'LIMIT_EXCEEDED'
  | 'DUPLICATE_ACTION'
  | 'MISSED_CANCELLATION'
  | 'PROHIBITED_ACTION';

export type BookingOutcome =
  | 'COMPLETED_CORRECTLY'
  | 'WRONG_BOOKING';

export type PurchaseOutcome =
  | 'COMPLETED_CORRECTLY'
  | 'WRONG_PURCHASE'
  | 'DUPLICATE_PURCHASE';

export type SubscriptionOperation =
  | 'CREATE_OR_RENEW'
  | 'CANCEL';