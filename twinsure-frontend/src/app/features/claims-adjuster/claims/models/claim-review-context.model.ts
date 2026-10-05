// Represents the authoritative eligibility and payment calculation.
// It is loaded independently from core claim details so a supporting
// service failure does not erase the complete claim workspace.

export interface PolicyCoverageRule {
  coverageId: number;
  productId: number;
  actionType: string;
  violationType: string;
  coverageLimit: number | null;
  active: boolean;
  description: string;
}

export interface PolicyExclusion {
  exclusionId: number;
  productId: number;
  exclusionCode: string;
  description: string;
  active: boolean;
}

export interface ClaimReviewContext {
  claimId: number;
  claimNumber: string;
  incidentId: number;
  actionId: number;
  policyId: number;
  customerId: number;
  twinId: number;

  actionType: string;
  incidentType: string;
  violationType: string | null;
  violationSeverity: string | null;

  actionDate: string;

  actionViolationFound: boolean;
  actionMarkedClaimEligible: boolean;

  customerMatches: boolean;
  twinMatches: boolean;
  policyValidOnActionDate: boolean;

  coverageMatched: boolean;

  activeExclusionsPresent: boolean;
  automaticExclusionDetected: boolean;
  manualExclusionReviewRequired: boolean;

  claimedAmount: number;
  reportedLossAmount: number;

  overallPolicyCoverageLimit: number;
  applicableCoverageLimit: number;

  policyDeductible: number;
  deductibleApplied: number;

  grossEligibleAmount: number;
  maximumPayableAmount: number;

  eligible: boolean;
  explanation: string;

  matchingCoverages: PolicyCoverageRule[];
  activeExclusions: PolicyExclusion[];
}
