// Matches the create and update request accepted by Customer Service.
// Both backend operations currently use the same profile fields.

export interface CustomerProfileRequest {
  fullName: string;
  email: string;
  phone: string;
  address: string;
}