// Matches the AI Twin registration request accepted by AI Twin Service.

import {
  AutonomyLevel
} from './ai-twin-types.model';

export interface CreateAiTwinRequest {
  twinName: string;
  providerName: string;
  modelName: string;
  autonomyLevel: AutonomyLevel;
  transactionLimit: number;
  approvalThreshold: number;
}