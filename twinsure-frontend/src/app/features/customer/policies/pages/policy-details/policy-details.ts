// Displays the complete insurance contract for one Customer-owned policy.
// Product terms, covered events, limits, exclusions, and compensation
// guidance are loaded through the ownership-protected policy endpoint.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { PolicyDetails as PolicyDetailsModel } from '../../models/policy-details.model';
import { PolicyApiService } from '../../services/policy-api.service';

@Component({
  selector: 'app-policy-details',

  imports: [CurrencyPipe, DatePipe, RouterLink],

  templateUrl: './policy-details.html',

  styleUrl: './policy-details.css',

  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PolicyDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly policyApi = inject(PolicyApiService);

  private readonly policyId = Number(this.route.snapshot.paramMap.get('policyId'));

  readonly policy = signal<PolicyDetailsModel | null>(null);

  readonly loading = signal(true);

  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    if (!Number.isInteger(this.policyId) || this.policyId <= 0) {
      this.router.navigateByUrl('/customer/policies');

      return;
    }

    this.loadPolicy();
  }

  reload(): void {
    this.loadPolicy();
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  statusClasses(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-emerald-100 text-emerald-700';

      case 'CANCELLED':
        return 'bg-red-100 text-red-700';

      case 'EXPIRED':
        return 'bg-slate-200 text-slate-700';

      default:
        return 'bg-blue-100 text-blue-700';
    }
  }

  coverageLimit(limit: number | null): number {
    return limit ?? this.policy()?.coverageLimit ?? 0;
  }

  private loadPolicy(): void {
    this.loading.set(true);

    this.error.set(null);

    this.policyApi
      .getPolicyDetails(this.policyId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (policy) => this.policy.set(policy),

        error: (error) => this.error.set(getApiErrorMessage(error)),
      });
  }
}
