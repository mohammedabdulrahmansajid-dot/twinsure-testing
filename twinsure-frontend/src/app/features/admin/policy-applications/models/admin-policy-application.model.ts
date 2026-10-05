// Represents policy applications returned to the Administrator.
// The model includes risk assessment, proposal, review, and acceptance data.

export type AdminPolicyApplicationStatus =
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'CHANGES_REQUESTED'
  | 'APPROVED'
  | 'REJECTED'
  | 'ACCEPTED'
  | 'DECLINED'
  | 'EXPIRED';

export type AdminRiskLevel =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH';

export type AdminUnderwritingRecommendation =
  | 'APPROVE'
  | 'REVIEW'
  | 'REJECT';

export interface AdminPolicyApplication {
  applicationId: number;
  customerId: number;
  twinId: number;
  productId: number;
  status: AdminPolicyApplicationStatus;
  riskScore: number | null;
  riskLevel: AdminRiskLevel | null;
  systemRecommendation: AdminUnderwritingRecommendation | null;
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