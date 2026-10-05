// Keeps authenticated users away from login and registration pages.
// Signed-in users are redirected to the dashboard for their role.

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Store } from '@ngrx/store';

import { combineLatest, filter, map, take } from 'rxjs';

import { getDashboardRoute } from '../constants/dashboard-route.util';
import { selectRestoring, selectUser } from '../../features/auth/state/auth.selectors';

export const guestGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);

  return combineLatest([store.select(selectUser), store.select(selectRestoring)]).pipe(
    filter(([, restoring]) => !restoring),
    take(1),
    map(([user]) => (user === null ? true : router.createUrlTree([getDashboardRoute(user.role)]))),
  );
};
