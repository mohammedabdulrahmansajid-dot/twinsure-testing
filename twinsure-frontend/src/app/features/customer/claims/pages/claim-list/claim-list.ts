// Displays formal claims owned by the authenticated Customer.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { Claim } from '../../models/claim.model';
import { ClaimApiService } from '../../services/claim-api.service';

@Component({
  selector: 'app-claim-list',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './claim-list.html',
  styleUrl: './claim-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClaimList implements OnInit {
  private readonly claimApi = inject(ClaimApiService);

  readonly claims = signal<Claim[]>([]);

  readonly loading = signal(true);

  readonly error = signal<string | null>(null);

  readonly loadingItems = [1, 2, 3];

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.loading.set(true);
    this.error.set(null);

    this.claimApi
      .getMyClaims()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (claims) =>
          this.claims.set(
            [...claims].sort(
              (first, second) =>
                new Date(second.submittedAt).getTime() - new Date(first.submittedAt).getTime(),
            ),
          ),

        error: (error) => this.error.set(getApiErrorMessage(error)),
      });
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }
}
