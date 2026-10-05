// Matches the formal claim request accepted by Claims Service.

export interface CreateClaimRequest {
  incidentId: number;
  claimedAmount: number;
}