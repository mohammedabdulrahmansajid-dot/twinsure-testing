// Defines the Identity Service contracts used by Admin user management.
// Passwords are accepted only during staff-account creation and are
// never returned or retained by the frontend.

export type AdminManagedRole =
  | 'CUSTOMER'
  | 'UNDERWRITER'
  | 'CLAIMS_ADJUSTER'
  | 'ADMIN';

export type StaffRole =
  | 'UNDERWRITER'
  | 'CLAIMS_ADJUSTER';

export type AdminUserStatus =
  | 'ACTIVE'
  | 'SUSPENDED';

export interface AdminUser {
  userId: number;
  username: string;
  role: AdminManagedRole;
  customerId: number | null;
  status: AdminUserStatus;
}

export interface CreateStaffUserRequest {
  username: string;
  password: string;
  role: StaffRole;
}

export interface UpdateUserStatusRequest {
  status: AdminUserStatus;
}