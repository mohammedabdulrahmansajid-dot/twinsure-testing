// Loads every evaluated AI action for Administrator oversight.
// AI Action Service remains responsible for simulation and evaluation.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { AdminAiAction } from '../models/admin-ai-action.model';

@Injectable({
  providedIn: 'root',
})
export class AdminAiActionApiService {
  private readonly http = inject(HttpClient);

  private readonly actionsUrl =
    `${environment.apiBaseUrl}/api/admin/ai-actions`;

  getAllActions(): Observable<AdminAiAction[]> {
    return this.http.get<AdminAiAction[]>(
      this.actionsUrl,
    );
  }
}