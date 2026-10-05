// Represents complete evidence for one Customer-owned AI action.
// The response includes scenario facts and every detected violation.

import {
  ActionStatus,
  ActionType,
  BookingOutcome,
  PurchaseOutcome,
  SubscriptionOperation
} from './ai-action-types.model';
import {
  ActionViolation
} from './action-violation.model';

export interface AiActionDetails {
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
  violations: ActionViolation[];
  occurredAt: string;
  evaluatedAt: string;
  createdAt: string;
}