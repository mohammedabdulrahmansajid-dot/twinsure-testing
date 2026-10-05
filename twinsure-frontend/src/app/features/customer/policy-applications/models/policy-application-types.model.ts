// Defines the exact application, risk, recommendation, and policy statuses.
// These values match the enums returned by Insurance and Policy Service.

export type PolicyApplicationStatus =
  | 'PENDING_REVIEW'
  | 'CHANGES_REQUIRED'
  | 'APPROVED'
  | 'REJECTED'
  | 'DECLINED'
  | 'ACCEPTED';

export type RiskLevel =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'VERY_HIGH';

export type UnderwritingRecommendation =
  | 'APPROVE'
  | 'MANUAL_REVIEW'
  | 'REQUEST_CHANGES'
  | 'REJECT';

export type PolicyStatus =
  | 'ACTIVE'
  | 'EXPIRED'
  | 'CANCELLED';