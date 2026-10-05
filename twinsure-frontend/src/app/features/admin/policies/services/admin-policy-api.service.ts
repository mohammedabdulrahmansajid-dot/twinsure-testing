// Provides Administrator policy oversight and status-management requests.
// Policy details reuse the existing complete Customer policy contract.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { PolicyDetails } from '../../../customer/policies/models/policy-details.model';
import {
  AdminPolicy,
  UpdatePolicyStatusRequest,
} from '../models/admin-policy.model';

@Injectable({
  providedIn: 'root',
})
export class AdminPolicyApiService {
  private readonly http = inject(HttpClient);

  private readonly adminPoliciesUrl =
    `${environment.apiBaseUrl}/api/admin/policies`;

  private readonly policiesUrl =
    `${environment.apiBaseUrl}/api/policies`;

  getAllPolicies(): Observable<AdminPolicy[]> {
    return this.http.get<AdminPolicy[]>(
      this.adminPoliciesUrl,
    );
  }

  getPolicyDetails(
    policyId: number,
  ): Observable<PolicyDetails> {
    return this.http.get<PolicyDetails>(
      `${this.policiesUrl}/${policyId}`,
    );
  }

  updatePolicyStatus(
    policyId: number,
    request: UpdatePolicyStatusRequest,
  ): Observable<AdminPolicy> {
    return this.http.put<AdminPolicy>(
      `${this.adminPoliciesUrl}/${policyId}/status`,
      request,
    );
  }
}