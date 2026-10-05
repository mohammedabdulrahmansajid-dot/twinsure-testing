// Combines the formal claim, source incident, evidence, and decisions.

import { Incident } from '../../incidents/models/incident.model';
import { ClaimDecision } from './claim-decision.model';
import { ClaimDocument } from './claim-document.model';
import { Claim } from './claim.model';

export interface ClaimDetails {
  claim: Claim;
  incident: Incident;
  documents: ClaimDocument[];
  decisions: ClaimDecision[];
}
