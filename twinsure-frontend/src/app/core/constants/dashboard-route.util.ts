// Maps each TwinSure role to its default authenticated dashboard.
// The same mapping is reused by login effects and route guards.

import { ApplicationRole } from './application-role';

export function getDashboardRoute(role: ApplicationRole): string {
  switch (role) {
    case 'CUSTOMER':
      return '/customer/dashboard';

    case 'UNDERWRITER':
      return '/underwriter/dashboard';

    case 'CLAIMS_ADJUSTER':
      return '/claims-adjuster/dashboard';

    case 'ADMIN':
      return '/admin/dashboard';
  }
}
