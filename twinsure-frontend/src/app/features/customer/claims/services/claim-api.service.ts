// Provides Customer claim and supporting-evidence operations through API Gateway.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { AddClaimDocumentRequest } from '../models/add-claim-document-request.model';
import { ClaimDetails } from '../models/claim-details.model';
import { ClaimDocument } from '../models/claim-document.model';
import { Claim } from '../models/claim.model';
import { CreateClaimRequest } from '../models/create-claim-request.model';

@Injectable({
  providedIn: 'root',
})
export class ClaimApiService {
  private readonly http = inject(HttpClient);

  private readonly claimsUrl = `${environment.apiBaseUrl}/api/claims`;

  createClaim(request: CreateClaimRequest): Observable<Claim> {
    return this.http.post<Claim>(this.claimsUrl, request);
  }

  getMyClaims(): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.claimsUrl}/my`);
  }

  getClaimDetails(claimId: number): Observable<ClaimDetails> {
    return this.http.get<ClaimDetails>(`${this.claimsUrl}/${claimId}`);
  }

  addDocument(claimId: number, request: AddClaimDocumentRequest): Observable<ClaimDocument> {
    return this.http.post<ClaimDocument>(`${this.claimsUrl}/${claimId}/documents`, request);
  }
}
