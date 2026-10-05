// Represents the extended AI Twin configuration returned by the
// claim-summary review endpoint, including provider and model details.

import { AiTwinPermission } from './ai-twin-permission.model';

export interface AiTwinClaimSummary {
  twinId: number;
  customerId: number;
  twinName: string;
  providerName: string;
  modelName: string;
  autonomyLevel: string;
  transactionLimit: number;
  approvalThreshold: number;
  status: string;
  permissions: AiTwinPermission[];
}
