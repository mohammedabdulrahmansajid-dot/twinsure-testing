// Provides Administrator claim oversight and assignment operations.
// Claims Service validates claim state and confirms that the selected
// user is an existing active Claims Adjuster before assignment.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { ClaimDetails } from '../../../customer/claims/models/claim-details.model';
import { AdminClaim, AssignClaimRequest, ClaimAssignmentResult } from '../models/admin-claim.model';

@Injectable({
  providedIn: 'root',
})
export class AdminClaimApiService {
  private readonly http = inject(HttpClient);

  private readonly adminClaimsUrl = `${environment.apiBaseUrl}/api/admin/claims`;

  private readonly claimsUrl = `${environment.apiBaseUrl}/api/claims`;

  getAllClaims(): Observable<AdminClaim[]> {
    return this.http.get<AdminClaim[]>(this.adminClaimsUrl);
  }

  getClaimDetails(claimId: number): Observable<ClaimDetails> {
    return this.http.get<ClaimDetails>(`${this.claimsUrl}/${claimId}`);
  }

  assignClaim(claimId: number, request: AssignClaimRequest): Observable<ClaimAssignmentResult> {
    return this.http.put<ClaimAssignmentResult>(
      `${this.adminClaimsUrl}/${claimId}/assign`,
      request,
    );
  }
}
    