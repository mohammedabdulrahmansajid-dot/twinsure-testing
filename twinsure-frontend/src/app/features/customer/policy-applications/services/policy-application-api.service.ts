// Provides typed Customer policy-application requests through the API Gateway.
// The credentials interceptor sends the HttpOnly authentication cookie.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { CreatePolicyApplicationRequest } from '../models/create-policy-application-request.model';
import { PolicyApplicationDetails } from '../models/policy-application-details.model';
import { PolicyApplicationResponse } from '../models/policy-application-response.model';
import { PolicyResponse } from '../models/policy-response.model';

@Injectable({
  providedIn: 'root',
})
export class PolicyApplicationApiService {
  private readonly http = inject(HttpClient);

  private readonly applicationsUrl = `${environment.apiBaseUrl}/api/policy-applications`;

  createApplication(
    request: CreatePolicyApplicationRequest,
  ): Observable<PolicyApplicationResponse> {
    return this.http.post<PolicyApplicationResponse>(this.applicationsUrl, request);
  }

  getMyApplications(): Observable<PolicyApplicationResponse[]> {
    return this.http.get<PolicyApplicationResponse[]>(`${this.applicationsUrl}/my`);
  }

  getApplicationDetails(applicationId: number): Observable<PolicyApplicationDetails> {
    return this.http.get<PolicyApplicationDetails>(`${this.applicationsUrl}/${applicationId}`);
  }

  acceptApplication(applicationId: number): Observable<PolicyResponse> {
    return this.http.post<PolicyResponse>(`${this.applicationsUrl}/${applicationId}/accept`, null);
  }

  declineApplication(applicationId: number): Observable<PolicyApplicationResponse> {
    return this.http.post<PolicyApplicationResponse>(
      `${this.applicationsUrl}/${applicationId}/decline`,
      null,
    );
  }

  resubmitApplication(applicationId: number): Observable<PolicyApplicationResponse> {
    return this.http.post<PolicyApplicationResponse>(
      `${this.applicationsUrl}/${applicationId}/resubmit`,
      null,
    );
  }
}
