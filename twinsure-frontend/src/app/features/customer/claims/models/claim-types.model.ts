// Defines the workflow, decision, and document values supported by Claims Service.

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

export type DocumentType =
  | 'INVOICE'
  | 'RECEIPT'
  | 'SCREENSHOT'
  | 'TRANSACTION_RECORD'
  | 'AI_ACTION_LOG'
  | 'CUSTOMER_STATEMENT'
  | 'OTHER';