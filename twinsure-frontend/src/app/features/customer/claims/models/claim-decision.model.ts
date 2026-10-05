// Represents one audited Claims Adjuster workflow decision.

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