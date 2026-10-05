// Displays complete evidence for one Customer-owned AI Action.
// Violations, insurance status, and claim eligibility determine
// whether the Customer can continue to incident reporting.

import {
  CurrencyPipe,
  DatePipe
} from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';
import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';

import {
  finalize
} from 'rxjs';

import {
  getApiErrorMessage
} from '../../../../../core/http/api-error.util';
import {
  AiActionDetails as AiActionDetailsModel
} from '../../models/ai-action-details.model';
import {
  AiActionApiService
} from '../../services/ai-action-api.service';

@Component({
  selector: 'app-ai-action-details',
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink
  ],
  templateUrl: './ai-action-details.html',
  styleUrl: './ai-action-details.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AiActionDetails implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly actionApi =
    inject(AiActionApiService);

  private readonly actionId =
    Number(
      this.route.snapshot.paramMap.get(
        'actionId'
      )
    );

  readonly action =
    signal<AiActionDetailsModel | null>(
      null
    );

  readonly loading =
    signal(true);

  readonly error =
    signal<string | null>(
      null
    );

  ngOnInit(): void {

    if (
      !Number.isInteger(this.actionId)
      || this.actionId <= 0
    ) {
      this.router.navigateByUrl(
        '/customer/ai-actions'
      );

      return;
    }

    this.loadAction();
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

  private loadAction(): void {

    this.loading.set(true);
    this.error.set(null);

    this.actionApi
      .getActionDetails(
        this.actionId
      )
      .pipe(
        finalize(() =>
          this.loading.set(false)
        )
      )
      .subscribe({
        next: action =>
          this.action.set(action),

        error: error =>
          this.error.set(
            getApiErrorMessage(error)
          )
      });
  }
}
