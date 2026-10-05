// Reuses the established Customer incident contract for Admin oversight.
// Claims Service remains authoritative for incident data and status.

import { Incident } from '../../../customer/incidents/models/incident.model';

export type AdminIncident = Incident;