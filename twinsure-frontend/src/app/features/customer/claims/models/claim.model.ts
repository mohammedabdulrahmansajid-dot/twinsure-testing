// Represents one formal claim in Customer claim-history responses.

import {
  ClaimStatus
} from './claim-types.model';

export interface Claim {
  claimId: number;
  claimNumber: string;
  incidentId: number;
  customerId: number;
  twinId: number;
  policyId: number;
  actionId: number;
  claimedAmount: number;
  approvedAmount: number | null;
  deductibleApplied: number | null;
  status: ClaimStatus;
  assignedAdjusterId: number | null;
  submittedAt: string;
  updatedAt: string;
  closedAt: string | null;
}