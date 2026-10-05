// Represents safe session information returned after authentication.
// The JWT is intentionally excluded because authentication uses
// the TWINSURE_TOKEN HttpOnly cookie.

import { ApplicationRole } from '../constants/application-role';

export interface AuthenticatedUser {
  userId: number;
  customerId: number | null;
  username: string;
  role: ApplicationRole;
}