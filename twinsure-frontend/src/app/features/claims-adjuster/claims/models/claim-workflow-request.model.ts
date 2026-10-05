// Defines request bodies used by Claims Adjuster workflow operations.
// Reason fields match the backend validation range of 5 to 500 characters.

export interface ClaimReasonRequest {
  reason: string;
}

export interface ClaimApprovalRequest {
  approvedAmount: number;
  reason: string;
}
