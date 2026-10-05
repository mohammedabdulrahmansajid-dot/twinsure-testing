// Extracts a readable message from backend or network errors.
// Feature effects and components use this function to present
// consistent errors without duplicating parsing logic.

import { HttpErrorResponse } from '@angular/common/http';

import { ApiErrorResponse } from '../models/api-error.model';

export function getApiErrorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'An unexpected error occurred.';
  }

  if (error.status === 0) {
    return 'The TwinSure platform is currently unreachable.';
  }

  const response = error.error as Partial<ApiErrorResponse> | null;

  if (
    response !== null &&
    typeof response.message === 'string' &&
    response.message.trim().length > 0
  ) {
    return response.message;
  }

  return getDefaultStatusMessage(error.status);
}

function getDefaultStatusMessage(status: number): string {
  switch (status) {
    case 400:
      return 'The request contains invalid information.';

    case 401:
      return 'Your session is missing or has expired.';

    case 403:
      return 'You are not allowed to perform this operation.';

    case 404:
      return 'The requested record could not be found.';

    case 409:
      return 'The operation conflicts with the current record state.';

    case 500:
      return 'The server encountered an unexpected error.';

    case 503:
      return 'A required TwinSure service is currently unavailable.';

    default:
      return 'The request could not be completed.';
  }
}
