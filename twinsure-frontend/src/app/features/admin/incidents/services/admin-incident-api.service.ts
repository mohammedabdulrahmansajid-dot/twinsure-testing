// Loads every reported incident for Administrator oversight.
// Incident creation and lifecycle changes remain owned by Claims Service.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { AdminIncident } from '../models/admin-incident.model';

@Injectable({
  providedIn: 'root',
})
export class AdminIncidentApiService {
  private readonly http = inject(HttpClient);

  private readonly incidentsUrl =
    `${environment.apiBaseUrl}/api/admin/incidents`;

  getAllIncidents(): Observable<AdminIncident[]> {
    return this.http.get<AdminIncident[]>(
      this.incidentsUrl,
    );
  }
}