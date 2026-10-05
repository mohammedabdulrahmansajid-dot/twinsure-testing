// Represents one Customer-reported incident in list responses.

import {
  IncidentStatus,
  IncidentType
} from './incident-types.model';

export interface Incident {
  incidentId: number;
  customerId: number;
  twinId: number;
  policyId: number;
  actionId: number;
  incidentType: IncidentType;
  description: string;
  lossAmount: number;
  actionOccurredAt: string;
  reportedAt: string;
  status: IncidentStatus;
}