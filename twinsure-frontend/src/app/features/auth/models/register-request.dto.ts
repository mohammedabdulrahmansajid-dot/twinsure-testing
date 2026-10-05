// Represents a public Customer account registration request.
// Role assignment remains a backend responsibility and is not accepted here.

export interface RegisterRequest {
  username: string;
  password: string;
}