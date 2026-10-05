// Displays policy applications owned by the authenticated Customer.
// The page loads shared application state through NgRx and provides
// navigation to individual underwriting and proposal details.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Store } from '@ngrx/store';

import { PolicyApplicationActions } from '../../state/policy-application.actions';
import {
  selectApplications,
  selectError,
  selectLoading,
} from '../../state/policy-application.selectors';

@Component({
  selector: 'app-policy-application-list',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './policy-application-list.html',
  styleUrl: './policy-application-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PolicyApplicationList implements OnInit {
  private readonly store = inject(Store);

  readonly applications = this.store.selectSignal(selectApplications);

  readonly loading = this.store.selectSignal(selectLoading);

  readonly error = this.store.selectSignal(selectError);

  readonly loadingItems = [1, 2, 3];

  ngOnInit(): void {
    this.loadApplications();
  }

  reload(): void {
    this.loadApplications();
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private loadApplications(): void {
    this.store.dispatch(PolicyApplicationActions.loadMyApplications());
  }
}
