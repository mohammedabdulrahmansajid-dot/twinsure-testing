// Provides typed Customer AI Action operations through the API Gateway.
// The global credentials interceptor automatically sends the HttpOnly JWT cookie.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { ActionEvaluationResponse } from '../models/action-evaluation-response.model';
import { AiActionDetails } from '../models/ai-action-details.model';
import { AiAction } from '../models/ai-action.model';
import { SimulateActionRequest } from '../models/simulate-action-request.model';

@Injectable({
  providedIn: 'root',
})
export class AiActionApiService {
  private readonly http = inject(HttpClient);

  private readonly actionsUrl = `${environment.apiBaseUrl}/api/ai-actions`;

  simulateAction(request: SimulateActionRequest): Observable<ActionEvaluationResponse> {
    return this.http.post<ActionEvaluationResponse>(`${this.actionsUrl}/simulate`, request);
  }

  getMyActions(): Observable<AiAction[]> {
    return this.http.get<AiAction[]>(`${this.actionsUrl}/my`);
  }

  getActionDetails(actionId: number): Observable<AiActionDetails> {
    return this.http.get<AiActionDetails>(`${this.actionsUrl}/${actionId}`);
  }
}
