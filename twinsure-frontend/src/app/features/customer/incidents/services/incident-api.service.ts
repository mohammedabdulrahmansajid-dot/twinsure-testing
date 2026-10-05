// Provides typed Customer incident operations through API Gateway.
// Authentication and ownership are enforced through the HttpOnly JWT cookie.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { IncidentDetails } from '../models/incident-details.model';
import { Incident } from '../models/incident.model';
import { ReportIncidentRequest } from '../models/report-incident-request.model';

@Injectable({
  providedIn: 'root',
})
export class IncidentApiService {
  private readonly http = inject(HttpClient);

  private readonly incidentsUrl = `${environment.apiBaseUrl}/api/incidents`;

  reportIncident(request: ReportIncidentRequest): Observable<Incident> {
    return this.http.post<Incident>(this.incidentsUrl, request);
  }

  getMyIncidents(): Observable<Incident[]> {
    return this.http.get<Incident[]>(`${this.incidentsUrl}/my`);
  }

  getIncidentDetails(incidentId: number): Observable<IncidentDetails> {
    return this.http.get<IncidentDetails>(`${this.incidentsUrl}/${incidentId}`);
  }
}
