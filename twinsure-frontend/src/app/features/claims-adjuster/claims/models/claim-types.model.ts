// Defines the supported claim, incident, evidence, and decision values.
// These types keep Claims Adjuster API and UI operations strongly typed.

export type ClaimStatus =
  | 'SUBMITTED'
  | 'ASSIGNED'
  | 'UNDER_REVIEW'
  | 'MORE_INFORMATION_REQUIRED'
  | 'APPROVED'
  | 'PARTIALLY_APPROVED'
  | 'REJECTED'
  | 'CLOSED';

export type ClaimDecisionType =
  | 'ASSIGN'
  | 'START_REVIEW'
  | 'REQUEST_INFORMATION'
  | 'APPROVE'
  | 'PARTIALLY_APPROVE'
  | 'REJECT'
  | 'CLOSE';

export type IncidentStatus =
  | 'REPORTED'
  | 'CLAIM_CREATED'
  | 'CLOSED';

export type IncidentType =
  | 'WRONG_ACTION'
  | 'APPROVAL_MISSING'
  | 'LIMIT_EXCEEDED'
  | 'DUPLICATE_ACTION'
  | 'MISSED_CANCELLATION'
  | 'PROHIBITED_ACTION';

export type DocumentType =
  | 'INVOICE'
  | 'RECEIPT'
  | 'SCREENSHOT'
  | 'TRANSACTION_RECORD'
  | 'AI_ACTION_LOG'
  | 'CUSTOMER_STATEMENT'
  | 'OTHER';

export type ClaimActionPanel =
  | 'START_REVIEW'
  | 'REQUEST_INFORMATION'
  | 'APPROVE'
  | 'PARTIALLY_APPROVE'
  | 'REJECT'
  | 'CLOSE'
  | null;