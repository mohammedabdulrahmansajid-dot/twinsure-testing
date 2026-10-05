// Displays AI Actions owned by the authenticated Customer.
// Signals manage this read-only history because the data belongs
// only to this page and does not require shared workflow state.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { AiAction } from '../../models/ai-action.model';
import { AiActionApiService } from '../../services/ai-action-api.service';

@Component({
  selector: 'app-ai-action-history',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './ai-action-history.html',
  styleUrl: './ai-action-history.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AiActionHistory implements OnInit {
  private readonly actionApi = inject(AiActionApiService);

  readonly actions = signal<AiAction[]>([]);

  readonly loading = signal(true);

  readonly error = signal<string | null>(null);

  readonly loadingItems = [1, 2, 3];

  ngOnInit(): void {
    this.loadActions();
  }

  loadActions(): void {
    this.loading.set(true);
    this.error.set(null);

    this.actionApi
      .getMyActions()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (actions) =>
          this.actions.set(
            [...actions].sort(
              (first, second) =>
                new Date(second.createdAt).getTime() - new Date(first.createdAt).getTime(),
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
