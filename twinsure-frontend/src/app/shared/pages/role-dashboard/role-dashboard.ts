// Displays a temporary authenticated dashboard while role features are built.
// It confirms the current session, role-based routing, and logout behavior.

import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Store } from '@ngrx/store';

import { AuthActions } from '../../../features/auth/state/auth.actions';
import { selectUser } from '../../../features/auth/state/auth.selectors';

@Component({
  selector: 'app-role-dashboard',
  templateUrl: './role-dashboard.html',
  styleUrl: './role-dashboard.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleDashboard {
  private readonly store = inject(Store);

  private readonly route = inject(ActivatedRoute);

  readonly user = this.store.selectSignal(selectUser);

  readonly title = signal(this.route.snapshot.data['title'] as string);

  readonly description = signal(this.route.snapshot.data['description'] as string);

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
