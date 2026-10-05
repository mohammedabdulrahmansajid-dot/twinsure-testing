// Represents the Customer-reported incident linked to a claim.
// The Adjuster uses the incident loss, description, type, and action date.

import {
  IncidentStatus,
  IncidentType
} from './claim-types.model';

export interface IncidentResponse {
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