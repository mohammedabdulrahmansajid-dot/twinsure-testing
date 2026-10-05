// Allows navigation only when safe session information is available.
// Backend services remain the final authority for authentication and access.

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Store } from '@ngrx/store';

import { combineLatest, filter, map, take } from 'rxjs';

import { selectAuthenticated, selectRestoring } from '../../features/auth/state/auth.selectors';

export const authGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);

  return combineLatest([store.select(selectAuthenticated), store.select(selectRestoring)]).pipe(
    filter(([, restoring]) => !restoring),
    take(1),
    map(([authenticated]) => (authenticated ? true : router.createUrlTree(['/login']))),
  );
};
