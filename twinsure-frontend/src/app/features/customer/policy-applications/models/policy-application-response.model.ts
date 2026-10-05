// Represents a policy application throughout submission,
// underwriting review, approval, rejection, and acceptance.

import {
  PolicyApplicationStatus,
  RiskLevel,
  UnderwritingRecommendation
} from './policy-application-types.model';

export interface PolicyApplicationResponse {
  applicationId: number;
  customerId: number;
  twinId: number;
  productId: number;
  status: PolicyApplicationStatus;
  riskScore: number | null;
  riskLevel: RiskLevel | null;
  systemRecommendation:
    UnderwritingRecommendation | null;
  proposedPremium: number | null;
  proposedCoverageLimit: number | null;
  proposedDeductible: number | null;
  reviewedBy: number | null;
  decisionReason: string | null;
  submittedAt: string;
  reviewedAt: string | null;
  proposalExpiresAt: string | null;
  acceptedAt: string | null;
}