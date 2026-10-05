// Matches the incident report request accepted by Claims Service.
// Customer identity is omitted because it is obtained from the JWT.

import {
  IncidentType
} from './incident-types.model';

export interface ReportIncidentRequest {
  actionId: number;
  policyId: number;
  incidentType: IncidentType;
  description: string;
  lossAmount: number;
}