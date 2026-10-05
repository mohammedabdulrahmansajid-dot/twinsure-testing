// Represents the active-policy protection applied to an AI Twin.
// The frontend uses this response to disable risk-sensitive changes.

export interface AiTwinConfigurationLock {
  twinId: number;
  locked: boolean;
  policyId: number | null;
  policyStatus: string | null;
  policyStartDate: string | null;
  policyEndDate: string | null;
  explanation: string;
}