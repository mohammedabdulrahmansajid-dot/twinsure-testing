// Represents a formal claim returned by Claims Service.
// It includes ownership, financial values, workflow state, and timestamps.

import {
  ClaimStatus
} from './claim-types.model';

export interface ClaimResponse {
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