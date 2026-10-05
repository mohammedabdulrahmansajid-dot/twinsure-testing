// Represents one audit entry in the claim decision history.
// It records the actor, transition, financial result, reason, and time.

import {
  ClaimDecisionType,
  ClaimStatus
} from './claim-types.model';

export interface ClaimDecision {
  decisionId: number;
  claimId: number;
  adjusterId: number;
  decisionType: ClaimDecisionType;
  previousStatus: ClaimStatus;
  newStatus: ClaimStatus;
  approvedAmount: number | null;
  reason: string;
  decidedAt: string;
}