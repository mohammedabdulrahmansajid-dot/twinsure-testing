// Matches the Customer profile returned by Customer Service.
// Date-time values arrive as ISO strings and are formatted in the template.

export type CustomerStatus =
  | 'ACTIVE'
  | 'SUSPENDED';

export interface CustomerProfileResponse {
  customerId: number;
  userId: number;
  fullName: string;
  email: string;
  phone: string;
  address: string;
  status: CustomerStatus;
  createdAt: string;
  updatedAt: string;
}