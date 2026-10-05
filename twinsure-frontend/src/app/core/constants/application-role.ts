// Defines the roles issued by TwinSure Identity Service.
// These values support typed role checks and route protection.

export const APPLICATION_ROLES = {
  CUSTOMER: 'CUSTOMER',
  UNDERWRITER: 'UNDERWRITER',
  CLAIMS_ADJUSTER: 'CLAIMS_ADJUSTER',
  ADMIN: 'ADMIN'
} as const;

export type ApplicationRole =
  typeof APPLICATION_ROLES[keyof typeof APPLICATION_ROLES];