// Provides Administrator AI Twin oversight through the API Gateway.
// Admin can retrieve every Twin and update operational status.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import {
  AdminAiTwin,
  UpdateAiTwinStatusRequest,
} from '../models/admin-ai-twin.model';

@Injectable({
  providedIn: 'root',
})
export class AdminAiTwinApiService {
  private readonly http = inject(HttpClient);

  private readonly aiTwinsUrl =
    `${environment.apiBaseUrl}/api/admin/ai-twins`;

  getAllAiTwins(): Observable<AdminAiTwin[]> {
    return this.http.get<AdminAiTwin[]>(
      this.aiTwinsUrl,
    );
  }

  updateAiTwinStatus(
    twinId: number,
    request: UpdateAiTwinStatusRequest,
  ): Observable<AdminAiTwin> {
    return this.http.put<AdminAiTwin>(
      `${this.aiTwinsUrl}/${twinId}/status`,
      request,
    );
  }
}