// Reuses the established Customer claim contracts for Admin oversight.
// Claims Service remains authoritative for assignment validation,
// workflow status, and the resulting audited assignment decision.

import { ClaimDecision } from '../../../customer/claims/models/claim-decision.model';
import { Claim } from '../../../customer/claims/models/claim.model';

export type AdminClaim = Claim;

export type AdminClaimStatus = Claim['status'];

export interface AssignClaimRequest {
  adjusterId: number;
}

export interface ClaimAssignmentResult {
  claim: Claim;
  decision: ClaimDecision;
}