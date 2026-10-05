// Represents a completed workflow mutation and the audit record it created.
// The frontend reloads full claim details after receiving this response.

import { ClaimDecision } from './claim-decision.model';
import { ClaimResponse } from './claim-response.model';

export interface ClaimDecisionResult {
  claim: ClaimResponse;
  decision: ClaimDecision;
}
