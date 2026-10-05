// Matches the complete simulation request accepted by AI Action Service.
// Only scenario fields relevant to the selected action type are populated.

import {
  ActionType,
  BookingOutcome,
  PurchaseOutcome,
  SubscriptionOperation
} from './ai-action-types.model';

export interface SimulateActionRequest {
  twinId: number;
  actionType: ActionType;
  transactionReference: string;
  description: string;
  actionAmount: number | null;
  approvalProvided: boolean;
  bookingOutcome: BookingOutcome | null;
  purchaseOutcome: PurchaseOutcome | null;
  subscriptionOperation: SubscriptionOperation | null;
  cancellationCompleted: boolean | null;
  occurredAt: string | null;
}
