// Matches the safe login response returned by Identity Service.
// The JWT is excluded because it is stored in an HttpOnly cookie.

import {
  ApplicationRole
} from '../../../core/constants/application-role';

export interface LoginResponse {
  userId: number;
  customerId: number | null;
  username: string;
  role: ApplicationRole;
  message: string;
}