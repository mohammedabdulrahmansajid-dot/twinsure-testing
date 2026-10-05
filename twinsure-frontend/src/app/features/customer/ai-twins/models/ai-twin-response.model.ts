  // Represents one Customer-owned AI Twin returned by AI Twin Service.

  import {
    AiTwinStatus,
    AutonomyLevel
  } from './ai-twin-types.model';

  export interface AiTwinResponse {
    twinId: number;
    customerId: number;
    twinName: string;
    providerName: string;
    modelName: string;
    autonomyLevel: AutonomyLevel;
    transactionLimit: number;
    approvalThreshold: number;
    status: AiTwinStatus;
  }