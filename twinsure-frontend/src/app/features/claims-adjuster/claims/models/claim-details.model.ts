// Combines the claim, incident, supporting evidence, and decision history.
// Claims Service returns this response for the assigned Adjuster workspace.

import { ClaimDecision } from './claim-decision.model';
import { ClaimDocument } from './claim-document.model';
import { ClaimResponse } from './claim-response.model';
import { IncidentResponse } from './incident-response.model';

export interface ClaimDetails {
  claim: ClaimResponse;
  incident: IncidentResponse;
  documents: ClaimDocument[];
  decisions: ClaimDecision[];
}
