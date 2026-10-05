// Defines the incident types and statuses supported by Claims Service.
// These values match the backend enums exactly.

export type IncidentType =
  | 'WRONG_ACTION'
  | 'APPROVAL_MISSING'
  | 'LIMIT_EXCEEDED'
  | 'DUPLICATE_ACTION'
  | 'MISSED_CANCELLATION'
  | 'PROHIBITED_ACTION';

export type IncidentStatus =
  | 'REPORTED'
  | 'CLAIM_CREATED'
  | 'CLOSED';