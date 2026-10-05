// Combines the stored incident with validated AI Action evidence.

import { AiActionEvidence } from './ai-action-evidence.model';
import { Incident } from './incident.model';

export interface IncidentDetails {
  incident: Incident;
  actionEvidence: AiActionEvidence;
}
