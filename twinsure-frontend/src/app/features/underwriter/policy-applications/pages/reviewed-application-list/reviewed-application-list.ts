// Displays applications previously reviewed by the logged-in Underwriter.
// The newest reviewed application appears first for quick audit access.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { PolicyApplicationResponse } from '../../../../customer/policy-applications/models/policy-application-response.model';
import { UnderwriterApplicationApiService } from '../../services/underwriter-application-api.service';

@Component({
  selector: 'app-reviewed-application-list',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './reviewed-application-list.html',
  styleUrl: './reviewed-application-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReviewedApplicationList implements OnInit {
  private readonly applicationApi = inject(UnderwriterApplicationApiService);

  readonly applications = signal<PolicyApplicationResponse[]>([]);

  readonly loading = signal(true);

  readonly error = signal<string | null>(null);

  readonly loadingItems = [1, 2, 3];

  ngOnInit(): void {
    this.loadApplications();
  }

  loadApplications(): void {
    this.loading.set(true);
    this.error.set(null);

    this.applicationApi
      .getReviewedApplications()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (applications) => {
          this.applications.set(
            [...applications].sort(
              (first, second) =>
                new Date(second.reviewedAt ?? second.submittedAt).getTime() -
                new Date(first.reviewedAt ?? first.submittedAt).getTime(),
            ),
          );
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
}
