// Ensures that an authenticated user enters only the route area
// assigned to the user's TwinSure role.

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Store } from '@ngrx/store';

import { filter, map, take } from 'rxjs';

import { ApplicationRole } from '../constants/application-role';
import { selectUser } from '../../features/auth/state/auth.selectors';

export function roleGuard(allowedRoles: readonly ApplicationRole[]): CanActivateFn {
  return () => {
    const store = inject(Store);
    const router = inject(Router);

    return store.select(selectUser).pipe(
      filter((user) => user !== null),
      take(1),
      map((user) =>
        allowedRoles.includes(user.role) ? true : router.createUrlTree(['/unauthorized']),
      ),
    );
  };
}
