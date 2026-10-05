// Defines Customer records and status updates used by Admin oversight.
// Customer Service remains authoritative for profile and status changes.

export type AdminCustomerStatus =
  | 'ACTIVE'
  | 'SUSPENDED';

export interface AdminCustomer {
  customerId: number;
  userId: number;
  fullName: string;
  email: string;
  phone: string;
  status: AdminCustomerStatus;
}

export interface UpdateCustomerStatusRequest {
  status: AdminCustomerStatus;
}