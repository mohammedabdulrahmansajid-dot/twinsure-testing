// Represents the issued policy returned after Customer proposal acceptance.

import {
  PolicyStatus
} from './policy-application-types.model';

export interface PolicyResponse {
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