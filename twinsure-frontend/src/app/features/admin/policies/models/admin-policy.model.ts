// Represents issued policies and administrative status changes.
// Admin may cancel or expire an active policy with a recorded reason.

export type AdminPolicyStatus =
  | 'ACTIVE'
  | 'CANCELLED'
  | 'EXPIRED';

export interface AdminPolicy {
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
  status: AdminPolicyStatus;
  cancellationReason: string | null;
}

export interface UpdatePolicyStatusRequest {
  status: Exclude<AdminPolicyStatus, 'ACTIVE'>;
  reason: string;
}