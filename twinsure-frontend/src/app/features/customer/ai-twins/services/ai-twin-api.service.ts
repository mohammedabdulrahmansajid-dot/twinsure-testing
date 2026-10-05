// Provides typed Customer AI Twin requests through the API Gateway.
// Authentication credentials are added by the global HTTP interceptor.
// Configuration-lock checks prevent edits while active insurance exists.

import {
  HttpClient
} from '@angular/common/http';
import {
  inject,
  Injectable
} from '@angular/core';
import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../../../environments/environment';
import {
  AiTwinConfigurationLock
} from '../models/ai-twin-configuration-lock.model';
import {
  AiTwinResponse
} from '../models/ai-twin-response.model';
import {
  CreateAiTwinRequest
} from '../models/create-ai-twin-request.model';
import {
  TwinPermissionRequest
} from '../models/twin-permission-request.model';
import {
  TwinPermissionResponse
} from '../models/twin-permission-response.model';
import {
  UpdateAiTwinRequest
} from '../models/update-ai-twin-request.model';

@Injectable({
  providedIn: 'root'
})
export class AiTwinApiService {

  private readonly http =
    inject(HttpClient);

  private readonly aiTwinsUrl =
    `${environment.apiBaseUrl}/api/ai-twins`;

  getMyAiTwins():
    Observable<AiTwinResponse[]> {

    return this.http.get<AiTwinResponse[]>(
      `${this.aiTwinsUrl}/my`
    );
  }

  getAiTwin(
    twinId: number
  ): Observable<AiTwinResponse> {

    return this.http.get<AiTwinResponse>(
      `${this.aiTwinsUrl}/${twinId}`
    );
  }

  getConfigurationLock(
    twinId: number
  ): Observable<AiTwinConfigurationLock> {

    return this.http.get<AiTwinConfigurationLock>(
      `${this.aiTwinsUrl}/${twinId}/configuration-lock`
    );
  }

  createAiTwin(
    request: CreateAiTwinRequest
  ): Observable<AiTwinResponse> {

    return this.http.post<AiTwinResponse>(
      this.aiTwinsUrl,
      request
    );
  }

  updateAiTwin(
    twinId: number,
    request: UpdateAiTwinRequest
  ): Observable<AiTwinResponse> {

    return this.http.put<AiTwinResponse>(
      `${this.aiTwinsUrl}/${twinId}`,
      request
    );
  }

  getPermissions(
    twinId: number
  ): Observable<TwinPermissionResponse[]> {

    return this.http.get<TwinPermissionResponse[]>(
      `${this.aiTwinsUrl}/${twinId}/permissions`
    );
  }

  configurePermission(
    twinId: number,
    request: TwinPermissionRequest
  ): Observable<TwinPermissionResponse> {

    return this.http.put<TwinPermissionResponse>(
      `${this.aiTwinsUrl}/${twinId}/permissions`,
      request
    );
  }
}