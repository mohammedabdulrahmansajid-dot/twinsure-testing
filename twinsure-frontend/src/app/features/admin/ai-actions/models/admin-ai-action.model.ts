// Represents evaluated AI actions returned to the Administrator.
// Scenario fields explain the simulated outcome and insurance context.

export type AdminActionType =
  | 'TRAVEL_BOOKING'
  | 'ONLINE_PURCHASE'
  | 'SUBSCRIPTION_MANAGEMENT';

export type AdminActionStatus =
  | 'COMPLIANT'
  | 'VIOLATION_DETECTED';

export type AdminBookingOutcome =
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELLED'
  | null;

export type AdminPurchaseOutcome =
  | 'COMPLETED'
  | 'FAILED'
  | 'REFUNDED'
  | null;

export type AdminSubscriptionOperation =
  | 'CREATED'
  | 'RENEWED'
  | 'CANCELLED'
  | null;

export interface AdminAiAction {
  actionId: number;
  customerId: number;
  twinId: number;
  policyId: number | null;
  actionType: AdminActionType;
  transactionReference: string;
  description: string;
  actionAmount: number;
  approvalProvided: boolean;
  bookingOutcome: AdminBookingOutcome;
  purchaseOutcome: AdminPurchaseOutcome;
  subscriptionOperation: AdminSubscriptionOperation;
  cancellationCompleted: boolean | null;
  actionStatus: AdminActionStatus;
  insured: boolean;
  occurredAt: string;
  evaluatedAt: string;
  createdAt: string;
}