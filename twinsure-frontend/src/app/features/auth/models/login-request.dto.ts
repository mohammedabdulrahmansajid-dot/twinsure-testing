// Represents credentials submitted to TwinSure Identity Service.
// The password exists only during the login request and is never stored.

export interface LoginRequest {
  username: string;
  password: string;
}