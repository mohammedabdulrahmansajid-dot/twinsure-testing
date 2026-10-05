// Displays issued policies owned by the authenticated Customer.
// Signals are sufficient because this is page-local, read-only data.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { Policy } from '../../models/policy.model';
import { PolicyApiService } from '../../services/policy-api.service';

@Component({
  selector: 'app-policy-list',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './policy-list.html',
  styleUrl: './policy-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PolicyList implements OnInit {
  private readonly policyApi = inject(PolicyApiService);

  readonly policies = signal<Policy[]>([]);

  readonly loading = signal(true);

  readonly error = signal<string | null>(null);

  readonly loadingItems = [1, 2];

  ngOnInit(): void {
    this.loadPolicies();
  }

  loadPolicies(): void {
    this.loading.set(true);
    this.error.set(null);

    this.policyApi
      .getMyPolicies()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (policies) =>
          this.policies.set(
            [...policies].sort(
              (first, second) =>
                new Date(second.startDate).getTime() - new Date(first.startDate).getTime(),
            ),
          ),

        error: (error) => this.error.set(getApiErrorMessage(error)),
      });
  }
}
