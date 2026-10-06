// Displays every submitted or processed claim for platform administration.
// Admin can assign submitted claims while Claims Service validates that
// the selected user is an existing active Claims Adjuster.

import {
  CurrencyPipe,
  DatePipe
} from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal
} from '@angular/core';
import {
  FormsModule
} from '@angular/forms';
import {
  RouterLink
} from '@angular/router';

import {
  finalize
} from 'rxjs';

import {
  getApiErrorMessage
} from '../../../../../core/http/api-error.util';
import {
  AdminClaim,
  AdminClaimStatus
} from '../../models/admin-claim.model';
import {
  AdminClaimApiService
} from '../../services/admin-claim-api.service';

type ClaimStatusFilter =
  | 'ALL'
  | AdminClaimStatus;

@Component({
  selector: 'app-admin-claim-list',

  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
    RouterLink
  ],

  templateUrl:
    './admin-claim-list.html',

  styleUrl:
    './admin-claim-list.css',

  changeDetection:
    ChangeDetectionStrategy.OnPush
})
export class AdminClaimList
  implements OnInit {

  private readonly claimApi =
    inject(AdminClaimApiService);

  readonly claims =
    signal<AdminClaim[]>([]);

  readonly loading =
    signal(true);

  readonly error =
    signal<string | null>(
      null
    );

  readonly success =
    signal<string | null>(
      null
    );

  readonly searchText =
    signal('');

  readonly statusFilter =
    signal<ClaimStatusFilter>(
      'ALL'
    );

  readonly assignmentClaimId =
    signal<number | null>(
      null
    );

  readonly assigningClaimId =
    signal<number | null>(
      null
    );

  readonly adjusterIds =
    signal<Record<number, number | null>>(
      {}
    );

  readonly statusOptions:
    ClaimStatusFilter[] = [
      'ALL',
      'SUBMITTED',
      'ASSIGNED',
      'UNDER_REVIEW',
      'MORE_INFORMATION_REQUIRED',
      'APPROVED',
      'PARTIALLY_APPROVED',
      'REJECTED',
      'CLOSED'
    ];

  readonly filteredClaims =
    computed(() => {

      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const status =
        this.statusFilter();

      return [...this.claims()]
        .filter(claim => {

          const searchMatches =
            search.length === 0
            || claim.claimNumber
              .toLowerCase()
              .includes(search)
            || claim.claimId
              .toString()
              .includes(search)
            || claim.customerId
              .toString()
              .includes(search)
            || claim.policyId
              .toString()
              .includes(search)
            || claim.twinId
              .toString()
              .includes(search)
            || (
              claim.assignedAdjusterId !== null
              && claim.assignedAdjusterId
                .toString()
                .includes(search)
            );

          const statusMatches =
            status === 'ALL'
            || claim.status === status;

          return searchMatches
            && statusMatches;
        })
        .sort(
          (first, second) =>
            new Date(
              second.updatedAt
            ).getTime()
            - new Date(
              first.updatedAt
            ).getTime()
        );
    });

  readonly submittedCount =
    computed(() =>
      this.claims()
        .filter(
          claim =>
            claim.status === 'SUBMITTED'
        )
        .length
    );

  readonly activeCount =
    computed(() =>
      this.claims()
        .filter(
          claim =>
            claim.status === 'ASSIGNED'
            || claim.status
              === 'UNDER_REVIEW'
            || claim.status
              === 'MORE_INFORMATION_REQUIRED'
        )
        .length
    );

  readonly completedCount =
    computed(() =>
      this.claims()
        .filter(
          claim =>
            claim.status === 'APPROVED'
            || claim.status
              === 'PARTIALLY_APPROVED'
            || claim.status
              === 'REJECTED'
            || claim.status
              === 'CLOSED'
        )
        .length
    );

  ngOnInit(): void {

    this.loadClaims();
  }

  reload(): void {

    this.loadClaims();
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(
      value
    );
  }

  updateStatusFilter(
    value: string
  ): void {

    this.statusFilter.set(
      value as ClaimStatusFilter
    );
  }

  clearFilters(): void {

    this.searchText.set('');

    this.statusFilter.set(
      'ALL'
    );
  }

  openAssignment(
    claim: AdminClaim
  ): void {

    if (
      claim.status !== 'SUBMITTED'
    ) {
      return;
    }

    this.error.set(
      null
    );

    this.success.set(
      null
    );

    this.assignmentClaimId.set(
      claim.claimId
    );

    this.adjusterIds.update(
      currentValues => ({
        ...currentValues,
        [claim.claimId]: null
      })
    );
  }

  cancelAssignment(): void {

    this.assignmentClaimId.set(
      null
    );
  }

 updateAdjusterId(
  claimId: number,
  value: number | string | null
): void {

  const normalizedValue =
    value === null || value === ''
      ? null
      : Number(value);

  this.adjusterIds.update(
    currentValues => ({
      ...currentValues,
      [claimId]: normalizedValue // <-- Fixed: correctly uses the claimId parameter as the key
    })
  );

  console.log('Adjuster Map:', this.adjusterIds());
}


  assignClaim(
    claim: AdminClaim
  ): void {

    this.error.set(
      null
    );

    this.success.set(
      null
    );

    if (
      claim.status !== 'SUBMITTED'
    ) {
      this.error.set(
        'Only a submitted claim can be assigned.'
      );

      return;
    }

    const adjusterId =
      this.adjusterIds()[
        claim.claimId
      ];

    if (
      adjusterId === null
      || adjusterId === undefined
      || !Number.isInteger(
        adjusterId
      )
      || adjusterId <= 0
    ) {
      this.error.set(
        'Enter a valid positive Claims Adjuster user ID.'
      );

      return;
    }

    this.assigningClaimId.set(
      claim.claimId
    );

    this.claimApi
      .assignClaim(
        claim.claimId,
        {
          adjusterId
        }
      )
      .pipe(
        finalize(() =>
          this.assigningClaimId.set(
            null
          )
        )
      )
      .subscribe({
        next: result => {

          this.claims.update(
            currentClaims =>
              currentClaims.map(
                existingClaim =>
                  existingClaim.claimId
                    === result.claim.claimId
                    ? result.claim
                    : existingClaim
              )
          );

          this.assignmentClaimId.set(
            null
          );

          this.adjusterIds.update(
            currentValues => ({
              ...currentValues,
              [claim.claimId]: null
            })
          );

          this.success.set(
            `Claim ${result.claim.claimNumber} was assigned to Claims Adjuster ${result.claim.assignedAdjusterId}.`
          );
        },

        error: error => {

          this.error.set(
            getApiErrorMessage(
              error
            )
          );
        }
      });
  }

  formatLabel(
    value: string
  ): string {

    return value
      .toLowerCase()
      .split('_')
      .map(part =>
        part.charAt(0).toUpperCase()
        + part.slice(1)
      )
      .join(' ');
  }

  statusClasses(
    status: AdminClaimStatus
  ): string {

    switch (status) {
      case 'SUBMITTED':
        return 'bg-amber-100 text-amber-800';

      case 'ASSIGNED':
        return 'bg-blue-100 text-blue-700';

      case 'UNDER_REVIEW':
      case 'MORE_INFORMATION_REQUIRED':
        return 'bg-violet-100 text-violet-700';

      case 'APPROVED':
      case 'PARTIALLY_APPROVED':
        return 'bg-emerald-100 text-emerald-700';

      case 'REJECTED':
        return 'bg-red-100 text-red-700';

      case 'CLOSED':
        return 'bg-slate-200 text-slate-700';

      default:
        return 'bg-slate-100 text-slate-700';
    }
  }

  private loadClaims(): void {

    this.loading.set(
      true
    );

    this.error.set(
      null
    );

    this.success.set(
      null
    );

    this.assignmentClaimId.set(
      null
    );

    this.claimApi
      .getAllClaims()
      .pipe(
        finalize(() =>
          this.loading.set(
            false
          )
        )
      )
      .subscribe({
        next: claims => {

          this.claims.set(
            claims
          );
        },

        error: error => {

          this.error.set(
            getApiErrorMessage(
              error
            )
          );
        }
      });
  }
}