// Loads Customer-owned policy lists and complete policy contracts.
// The backend verifies ownership using the authenticated JWT claims.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { PolicyDetails } from '../models/policy-details.model';
import { Policy } from '../models/policy.model';

@Injectable({
  providedIn: 'root',
})
export class PolicyApiService {
  private readonly http = inject(HttpClient);

  private readonly policiesUrl = `${environment.apiBaseUrl}/api/policies`;

  getMyPolicies(): Observable<Policy[]> {
    return this.http.get<Policy[]>(`${this.policiesUrl}/my`);
  }

  getPolicyDetails(policyId: number): Observable<PolicyDetails> {
    return this.http.get<PolicyDetails>(`${this.policiesUrl}/${policyId}`);
  }
}
