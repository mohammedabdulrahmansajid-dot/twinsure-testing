// Represents the immediate backend result after simulating an AI action.
// The response summarizes compliance, insurance, and detected violations.

import {
  ActionStatus,
  ActionType
} from './ai-action-types.model';
import {
  ActionViolation
} from './action-violation.model';

export interface ActionEvaluationResponse {
  actionId: number;
  customerId: number;
  twinId: number;
  policyId: number | null;
  actionType: ActionType;
  transactionReference: string;
  actionStatus: ActionStatus;
  insured: boolean;
  compliant: boolean;
  claimEligible: boolean;
  violations: ActionViolation[];
  message: string;
}