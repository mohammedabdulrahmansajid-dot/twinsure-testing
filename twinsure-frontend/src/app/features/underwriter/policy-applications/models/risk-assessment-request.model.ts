// Carries additional incident history and remarks for risk assessment.
// AI Twin configuration is loaded independently by the backend.

export interface RiskAssessmentRequest {
  previousIncidentCount: number;
  assessmentRemarks: string | null;
}