// Represents the complete issued-policy contract returned by the backend.
// Includes product terms, covered events, exclusions, and the immutable
// AI Twin configuration captured when the policy was issued.

import {
  PolicyStatus
} from '../../policy-applications/models/policy-application-types.model';

export type PolicyActionType =
  | 'TRAVEL_BOOKING'
  | 'ONLINE_PURCHASE'
  | 'SUBSCRIPTION_MANAGEMENT';

export type PolicyViolationType =
  | 'WRONG_ACTION'
  | 'APPROVAL_MISSING'
  | 'LIMIT_EXCEEDED'
  | 'DUPLICATE_ACTION'
  | 'MISSED_CANCELLATION'
  | 'PROHIBITED_ACTION';

export interface PolicyCoverageRule {
  coverageId: number;
  productId: number;
  actionType: PolicyActionType;
  violationType: PolicyViolationType;
  coverageLimit: number | null;
  active: boolean;
  description: string;
}

export interface PolicyExclusion {
  exclusionId: number;
  productId: number;
  exclusionCode: string;
  description: string;
  active: boolean;
}

export interface PolicyPermissionSnapshot {
  actionType: string;
  permissionLevel: string;
  actionLimit: number | null;
  active: boolean;
}

export interface PolicyTwinSnapshot {
  twinId: number;
  twinName: string | null;
  autonomyLevel: string | null;
  transactionLimit: number | null;
  approvalThreshold: number | null;
  twinStatus: string | null;
  capturedAt: string | null;
  permissions: PolicyPermissionSnapshot[];
}

export interface PolicyDetails {
  policyId: number;
  policyNumber: string;
  applicationId: number;
  customerId: number;
  twinId: number;
  productId: number;

  productCode: string;
  productName: string;
  productDescription: string;

  premium: number;
  coverageLimit: number;
  deductible: number;

  startDate: string;
  endDate: string;
  status: PolicyStatus;

  cancellationReason: string | null;

  coverages: PolicyCoverageRule[];
  exclusions: PolicyExclusion[];

  twinSnapshot: PolicyTwinSnapshot | null;

  createdAt: string;
  updatedAt: string;
}