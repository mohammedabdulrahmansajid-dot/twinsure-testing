// Represents one evaluated AI action in Customer history.
// Scenario fields preserve the facts used during rule evaluation.

import {
  ActionStatus,
  ActionType,
  BookingOutcome,
  PurchaseOutcome,
  SubscriptionOperation
} from './ai-action-types.model';

export interface AiAction {
  actionId: number;
  customerId: number;
  twinId: number;
  policyId: number | null;
  actionType: ActionType;
  transactionReference: string;
  description: string;
  actionAmount: number | null;
  approvalProvided: boolean;
  bookingOutcome: BookingOutcome | null;
  purchaseOutcome: PurchaseOutcome | null;
  subscriptionOperation: SubscriptionOperation | null;
  cancellationCompleted: boolean | null;
  actionStatus: ActionStatus;
  insured: boolean;
  occurredAt: string;
  evaluatedAt: string;
  createdAt: string;
}