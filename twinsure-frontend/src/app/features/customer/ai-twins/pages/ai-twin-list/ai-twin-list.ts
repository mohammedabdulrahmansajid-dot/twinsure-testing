// Displays AI Twins owned by the authenticated Customer.
// The component dispatches a load action and consumes AI Twin
// state through NgRx selectors without calling the API directly.

import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { Store } from '@ngrx/store';

import { AiTwinActions } from '../../state/ai-twin.actions';
import { selectAiTwins, selectError, selectLoading } from '../../state/ai-twin.selectors';

@Component({
  selector: 'app-ai-twin-list',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './ai-twin-list.html',
  styleUrl: './ai-twin-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AiTwinList implements OnInit {
  private readonly store = inject(Store);

  readonly aiTwins = this.store.selectSignal(selectAiTwins);

  readonly loading = this.store.selectSignal(selectLoading);

  readonly error = this.store.selectSignal(selectError);

  readonly loadingItems = [1, 2, 3];

  ngOnInit(): void {
    this.loadAiTwins();
  }

  reload(): void {
    this.loadAiTwins();
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private loadAiTwins(): void {
    this.store.dispatch(AiTwinActions.loadMyAiTwins());
  }
}
