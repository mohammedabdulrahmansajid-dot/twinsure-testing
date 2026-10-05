// Matches the Customer policy-application request.
// Customer identity is intentionally excluded because it comes from JWT.

export interface CreatePolicyApplicationRequest {
  twinId: number;
  productId: number;
}