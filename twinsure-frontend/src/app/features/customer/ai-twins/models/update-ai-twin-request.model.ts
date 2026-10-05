// Matches the editable AI Twin configuration accepted by the backend.
// Twin name, provider, and model remain immutable after registration.

import {
  AutonomyLevel
} from './ai-twin-types.model';

export interface UpdateAiTwinRequest {
  autonomyLevel: AutonomyLevel;
  transactionLimit: number;
  approvalThreshold: number;
}