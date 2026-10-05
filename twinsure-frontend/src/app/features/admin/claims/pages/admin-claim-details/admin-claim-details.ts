// Displays a complete claim for Administrator oversight.
// Submitted claims can be assigned from this page, while evidence
// and decision history remain read-only for the Administrator.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { ClaimDetails } from '../../../../customer/claims/models/claim-details.model';
import { AdminClaimApiService } from '../../services/admin-claim-api.service';

@Component({
  selector: 'app-admin-claim-details',

  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink],

  templateUrl: './admin-claim-details.html',

  styleUrl: './admin-claim-details.css',

  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminClaimDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly claimApi = inject(AdminClaimApiService);

  private readonly claimId = Number(this.route.snapshot.paramMap.get('claimId'));

  readonly details = signal<ClaimDetails | null>(null);

  readonly loading = signal(true);

  readonly assigning = signal(false);

  readonly error = signal<string | null>(null);

  readonly success = signal<string | null>(null);

  readonly adjusterId = signal<number | null>(null);

  ngOnInit(): void {
    if (!Number.isInteger(this.claimId) || this.claimId <= 0) {
      this.router.navigateByUrl('/admin/claims');

      return;
    }

    this.loadClaim();
  }

  reload(): void {
    this.loadClaim();
  }

  updateAdjusterId(value: number | string | null): void {
    const normalizedValue = value === null || value === '' ? null : Number(value);

    this.adjusterId.set(normalizedValue);
  }

  assignClaim(): void {
    this.error.set(null);

    this.success.set(null);

    const currentDetails = this.details();

    if (currentDetails === null || currentDetails.claim.status !== 'SUBMITTED') {
      this.error.set('Only a submitted claim can be assigned.');

      return;
    }

    const selectedAdjusterId = this.adjusterId();

    if (
      selectedAdjusterId === null ||
      !Number.isInteger(selectedAdjusterId) ||
      selectedAdjusterId <= 0
    ) {
      this.error.set('Enter a valid positive Claims Adjuster user ID.');

      return;
    }

    this.assigning.set(true);

    this.claimApi
      .assignClaim(this.claimId, {
        adjusterId: selectedAdjusterId,
      })
      .pipe(finalize(() => this.assigning.set(false)))
      .subscribe({
        next: (result) => {
          const existingDetails = this.details();

          if (existingDetails !== null) {
            this.details.set({
              ...existingDetails,

              claim: result.claim,

              decisions: [result.decision, ...existingDetails.decisions],
            });
          }

          this.success.set(
            `Claim ${result.claim.claimNumber} was assigned to Claims Adjuster ${result.claim.assignedAdjusterId}.`,
          );

          this.adjusterId.set(null);
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private loadClaim(): void {
    this.loading.set(true);

    this.error.set(null);

    this.success.set(null);

    this.claimApi
      .getClaimDetails(this.claimId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (details) => {
          this.details.set({
            ...details,

            documents: [...details.documents].sort(
              (first, second) =>
                new Date(second.uploadedAt).getTime() - new Date(first.uploadedAt).getTime(),
            ),

            decisions: [...details.decisions].sort(
              (first, second) =>
                new Date(second.decidedAt).getTime() - new Date(first.decidedAt).getTime(),
            ),
          });
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }
}
