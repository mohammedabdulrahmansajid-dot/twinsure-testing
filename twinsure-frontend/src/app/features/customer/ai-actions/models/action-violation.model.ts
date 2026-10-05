// Represents one rule violation detected during AI Action evaluation.
// Claim eligibility indicates whether incident reporting may continue.

import {
  ViolationSeverity,
  ViolationType
} from './ai-action-types.model';

export interface ActionViolation {
  violationId: number;
  actionId: number;
  violationType: ViolationType;
  severity: ViolationSeverity;
  description: string;
  claimEligible: boolean;
  createdAt: string;
}
