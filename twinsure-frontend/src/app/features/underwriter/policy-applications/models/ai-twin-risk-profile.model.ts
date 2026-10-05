// Represents the AI Twin configuration used during underwriting assessment.

import {
  AiTwinPermission
} from './ai-twin-permission.model';

export interface AiTwinRiskProfile {
  twinId: number;
  customerId: number;
  twinName: string;
  autonomyLevel: string;
  transactionLimit: number;
  approvalThreshold: number;
  status: string;
  permissions: AiTwinPermission[];
}