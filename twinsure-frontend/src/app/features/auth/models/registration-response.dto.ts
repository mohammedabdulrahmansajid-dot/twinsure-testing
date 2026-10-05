// Matches the account information returned after Customer registration.
// Passwords and JWT values are intentionally absent from this response.

import {
  ApplicationRole
} from '../../../core/constants/application-role';

export type UserStatus =
  | 'ACTIVE'
  | 'SUSPENDED';

export interface RegistrationResponse {
  userId: number;
  username: string;
  role: ApplicationRole;
  customerId: number | null;
  status: UserStatus;
}
