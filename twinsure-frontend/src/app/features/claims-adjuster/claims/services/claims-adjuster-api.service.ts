// Provides typed Claims Adjuster requests through the API Gateway.
// The credentials interceptor sends the TwinSure HttpOnly authentication cookie.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { ClaimDecisionResult } from '../models/claim-decision-result.model';
import { ClaimDetails } from '../models/claim-details.model';
import { ClaimResponse } from '../models/claim-response.model';
import { ClaimReviewContext } from '../models/claim-review-context.model';
import { ClaimApprovalRequest, ClaimReasonRequest } from '../models/claim-workflow-request.model';

@Injectable({
  providedIn: 'root',
})
export class ClaimsAdjusterApiService {
  private readonly http = inject(HttpClient);

  private readonly claimsUrl = `${environment.apiBaseUrl}/api/claims`;

  private readonly adjusterClaimsUrl = `${environment.apiBaseUrl}/api/claims-adjuster/claims`;

  getMyAssignedClaims(): Observable<ClaimResponse[]> {
    return this.http.get<ClaimResponse[]>(`${this.adjusterClaimsUrl}/my`);
  }

  getClaimDetails(claimId: number): Observable<ClaimDetails> {
    return this.http.get<ClaimDetails>(`${this.claimsUrl}/${claimId}`);
  }

  getReviewContext(claimId: number): Observable<ClaimReviewContext> {
    return this.http.get<ClaimReviewContext>(`${this.adjusterClaimsUrl}/${claimId}/review-context`);
  }

  startReview(claimId: number, request: ClaimReasonRequest): Observable<ClaimDecisionResult> {
    return this.http.put<ClaimDecisionResult>(
      `${this.adjusterClaimsUrl}/${claimId}/start-review`,
      request,
    );
  }

  requestInformation(
    claimId: number,
    request: ClaimReasonRequest,
  ): Observable<ClaimDecisionResult> {
    return this.http.put<ClaimDecisionResult>(
      `${this.adjusterClaimsUrl}/${claimId}/request-information`,
      request,
    );
  }

  approveClaim(claimId: number, request: ClaimApprovalRequest): Observable<ClaimDecisionResult> {
    return this.http.put<ClaimDecisionResult>(
      `${this.adjusterClaimsUrl}/${claimId}/approve`,
      request,
    );
  }

  partiallyApproveClaim(
    claimId: number,
    request: ClaimApprovalRequest,
  ): Observable<ClaimDecisionResult> {
    return this.http.put<ClaimDecisionResult>(
      `${this.adjusterClaimsUrl}/${claimId}/partial-approve`,
      request,
    );
  }

  rejectClaim(claimId: number, request: ClaimReasonRequest): Observable<ClaimDecisionResult> {
    return this.http.put<ClaimDecisionResult>(
      `${this.adjusterClaimsUrl}/${claimId}/reject`,
      request,
    );
  }

  closeClaim(claimId: number, request: ClaimReasonRequest): Observable<ClaimDecisionResult> {
    return this.http.put<ClaimDecisionResult>(
      `${this.adjusterClaimsUrl}/${claimId}/close`,
      request,
    );
  }
}
