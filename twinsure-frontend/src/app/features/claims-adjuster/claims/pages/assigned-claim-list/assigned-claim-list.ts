// Displays claims assigned to the authenticated Claims Adjuster.
// Active work is separated from completed claims, and recent
// activity appears first within each workflow-priority group.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Store } from '@ngrx/store';

import { ClaimStatus } from '../../models/claim-types.model';
import { ClaimsAdjusterActions } from '../../state/claims-adjuster.actions';
import {
  selectActiveClaims,
  selectAssignedClaims,
  selectAssignedCount,
  selectCompletedClaims,
  selectCompletedCount,
  selectError,
  selectInformationRequiredCount,
  selectLoading,
  selectUnderReviewCount,
} from '../../state/claims-adjuster.selectors';

@Component({
  selector: 'app-assigned-claim-list',

  imports: [CurrencyPipe, DatePipe, RouterLink],

  templateUrl: './assigned-claim-list.html',

  styleUrl: './assigned-claim-list.css',

  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AssignedClaimList implements OnInit {
  private readonly store = inject(Store);

  readonly claims = this.store.selectSignal(selectAssignedClaims);

  readonly activeClaims = this.store.selectSignal(selectActiveClaims);

  readonly completedClaims = this.store.selectSignal(selectCompletedClaims);

  readonly assignedCount = this.store.selectSignal(selectAssignedCount);

  readonly underReviewCount = this.store.selectSignal(selectUnderReviewCount);

  readonly informationRequiredCount = this.store.selectSignal(selectInformationRequiredCount);

  readonly completedCount = this.store.selectSignal(selectCompletedCount);

  readonly loading = this.store.selectSignal(selectLoading);

  readonly error = this.store.selectSignal(selectError);

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.store.dispatch(ClaimsAdjusterActions.loadAssignedClaims());
  }

  statusLabel(status: ClaimStatus): string {
    switch (status) {
      case 'ASSIGNED':
        return 'Ready to Start';

      case 'UNDER_REVIEW':
        return 'Under Review';

      case 'MORE_INFORMATION_REQUIRED':
        return 'Waiting for Information';

      case 'PARTIALLY_APPROVED':
        return 'Partially Approved';

      default:
        return this.formatLabel(status);
    }
  }

  actionLabel(status: ClaimStatus): string {
    switch (status) {
      case 'ASSIGNED':
        return 'Start review';

      case 'UNDER_REVIEW':
        return 'Continue review';

      case 'MORE_INFORMATION_REQUIRED':
        return 'Review response';

      case 'APPROVED':
      case 'PARTIALLY_APPROVED':
      case 'REJECTED':
        return 'Review decision';

      case 'CLOSED':
        return 'View history';

      default:
        return 'View claim';
    }
  }

  statusClasses(status: ClaimStatus): string {
    switch (status) {
      case 'ASSIGNED':
        return 'bg-blue-100 text-blue-700';

      case 'UNDER_REVIEW':
        return 'bg-violet-100 text-violet-700';

      case 'MORE_INFORMATION_REQUIRED':
        return 'bg-amber-100 text-amber-800';

      case 'APPROVED':
        return 'bg-emerald-100 text-emerald-700';

      case 'PARTIALLY_APPROVED':
        return 'bg-cyan-100 text-cyan-800';

      case 'REJECTED':
        return 'bg-red-100 text-red-700';

      case 'CLOSED':
        return 'bg-slate-200 text-slate-700';

      default:
        return 'bg-slate-100 text-slate-700';
    }
  }

  private formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }
}
