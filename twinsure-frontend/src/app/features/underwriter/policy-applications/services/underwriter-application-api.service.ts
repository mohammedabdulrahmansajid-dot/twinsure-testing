// Provides Underwriter policy-application and AI Twin review operations.
// All requests travel through the Gateway with the HttpOnly JWT cookie.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { PolicyApplicationDetails } from '../../../customer/policy-applications/models/policy-application-details.model';
import { PolicyApplicationResponse } from '../../../customer/policy-applications/models/policy-application-response.model';
// import { AiTwinClaimSummary } from '../models/ai-twin-claim-summary.model';
import { AiTwinRiskProfile } from '../models/ai-twin-risk-profile.model';
import { PolicyApprovalRequest } from '../models/policy-approval-request.model';
import { PolicyReasonRequest } from '../models/policy-reason-request.model';
import { RiskAssessmentRequest } from '../models/risk-assessment-request.model';

@Injectable({
  providedIn: 'root',
})
export class UnderwriterApplicationApiService {
  private readonly http = inject(HttpClient);

  private readonly applicationsUrl = `${environment.apiBaseUrl}/api/policy-applications`;

  private readonly aiTwinsUrl = `${environment.apiBaseUrl}/api/ai-twins`;

  getPendingApplications(): Observable<PolicyApplicationResponse[]> {
    return this.http.get<PolicyApplicationResponse[]>(`${this.applicationsUrl}/pending`);
  }

  getReviewedApplications(): Observable<PolicyApplicationResponse[]> {
    return this.http.get<PolicyApplicationResponse[]>(`${this.applicationsUrl}/reviewed-by-me`);
  }

  getApplicationDetails(applicationId: number): Observable<PolicyApplicationDetails> {
    return this.http.get<PolicyApplicationDetails>(`${this.applicationsUrl}/${applicationId}`);
  }

  getRiskProfile(twinId: number): Observable<AiTwinRiskProfile> {
    return this.http.get<AiTwinRiskProfile>(`${this.aiTwinsUrl}/${twinId}/risk-profile`);
  }

//   getClaimSummary(twinId: number): Observable<AiTwinClaimSummary> {
//     return this.http.get<AiTwinClaimSummary>(`${this.aiTwinsUrl}/${twinId}/claim-summary`);
//   }

  assessApplication(
    applicationId: number,
    request: RiskAssessmentRequest,
  ): Observable<PolicyApplicationResponse> {
    return this.http.post<PolicyApplicationResponse>(
      `${this.applicationsUrl}/${applicationId}/assessment`,
      request,
    );
  }

  approveApplication(
    applicationId: number,
    request: PolicyApprovalRequest,
  ): Observable<PolicyApplicationResponse> {
    return this.http.post<PolicyApplicationResponse>(
      `${this.applicationsUrl}/${applicationId}/approve`,
      request,
    );
  }

  requestChanges(
    applicationId: number,
    request: PolicyReasonRequest,
  ): Observable<PolicyApplicationResponse> {
    return this.http.post<PolicyApplicationResponse>(
      `${this.applicationsUrl}/${applicationId}/request-changes`,
      request,
    );
  }

  rejectApplication(
    applicationId: number,
    request: PolicyReasonRequest,
  ): Observable<PolicyApplicationResponse> {
    return this.http.post<PolicyApplicationResponse>(
      `${this.applicationsUrl}/${applicationId}/reject`,
      request,
    );
  }
}
