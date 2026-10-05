// Represents complete AI Action evidence attached to Incident Details.

import {
  ActionViolationEvidence
} from './action-violation-evidence.model';

export interface AiActionEvidence {
  actionId: number;
  customerId: number;
  twinId: number;
  policyId: number | null;
  actionType: string;
  transactionReference: string;
  description: string;
  actionAmount: number | null;
  approvalProvided: boolean;
  actionStatus: string;
  insured: boolean;
  violations: ActionViolationEvidence[];
  occurredAt: string;
  evaluatedAt: string;
  createdAt: string;
}
