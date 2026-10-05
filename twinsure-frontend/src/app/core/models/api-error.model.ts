// Matches the standard JSON error structure returned by backend services.
// Typed errors allow forms and pages to display meaningful messages.

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}