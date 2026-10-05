// Loads all policy applications for Administrator oversight.
// Underwriting decisions remain restricted to the Underwriter workspace.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { AdminPolicyApplication } from '../models/admin-policy-application.model';

@Injectable({
  providedIn: 'root',
})
export class AdminPolicyApplicationApiService {
  private readonly http = inject(HttpClient);

  private readonly applicationsUrl =
    `${environment.apiBaseUrl}/api/admin/policy-applications`;

  getAllApplications(): Observable<AdminPolicyApplication[]> {
    return this.http.get<AdminPolicyApplication[]>(
      this.applicationsUrl,
    );
  }
}