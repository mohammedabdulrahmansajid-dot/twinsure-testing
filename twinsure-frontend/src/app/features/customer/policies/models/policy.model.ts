// Represents an issued Customer policy returned by Insurance and Policy Service.
// Date-only backend values arrive as ISO-formatted strings.

import {
  PolicyStatus
} from '../../policy-applications/models/policy-application-types.model';

export interface Policy {
  policyId: number;
  policyNumber: string;
  applicationId: number;
  customerId: number;
  twinId: number;
  productId: number;
  premium: number;
  coverageLimit: number;
  deductible: number;
  startDate: string;
  endDate: string;
  status: PolicyStatus;
  cancellationReason: string | null;
}