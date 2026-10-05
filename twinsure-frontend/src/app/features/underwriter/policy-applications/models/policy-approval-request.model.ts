// Carries the final policy proposal selected by the Underwriter.
// The approved values become the issued policy terms after acceptance.

export interface PolicyApprovalRequest {
  proposedPremium: number;
  proposedCoverageLimit: number;
  proposedDeductible: number;
  reason: string;
}