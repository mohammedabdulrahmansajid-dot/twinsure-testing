// Defines AI Twin records and status changes used by Admin oversight.
// Risk controls remain visible, while status changes are persisted
// by AI Twin Service.

export type AdminAiTwinStatus =
  | 'ACTIVE'
  | 'SUSPENDED'
  | 'RETIRED';

export type AdminAutonomyLevel =
  | 'ASSISTIVE'
  | 'SUPERVISED'
  | 'AUTONOMOUS';

export interface AdminAiTwin {
  twinId: number;
  customerId: number;
  twinName: string;
  providerName: string;
  modelName: string;
  autonomyLevel: AdminAutonomyLevel;
  transactionLimit: number;
  approvalThreshold: number;
  status: AdminAiTwinStatus;
}

export interface UpdateAiTwinStatusRequest {
  status: AdminAiTwinStatus;
}
``