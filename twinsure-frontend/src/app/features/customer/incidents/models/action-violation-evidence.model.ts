// Represents one AI Action violation included in incident evidence.

export interface ActionViolationEvidence {
  violationId: number;
  actionId: number;
  violationType: string;
  severity: string;
  description: string;
  claimEligible: boolean;
  createdAt: string;
}