// Persists safe user-display information between browser refreshes.
// The JWT remains exclusively in the TWINSURE_TOKEN HttpOnly cookie.

import { Injectable } from '@angular/core';

import {
  AuthenticatedUser
} from '../../../core/models/authenticated-user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthSessionService {

  private readonly storageKey =
    'twinsure_authenticated_user';

  saveUser(
    user: AuthenticatedUser
  ): void {

    localStorage.setItem(
      this.storageKey,
      JSON.stringify(user)
    );
  }

  loadUser(): AuthenticatedUser | null {

    const storedValue =
      localStorage.getItem(this.storageKey);

    if (storedValue === null) {
      return null;
    }

    try {
      return JSON.parse(
        storedValue
      ) as AuthenticatedUser;
    } catch {
      this.clear();

      return null;
    }
  }

  clear(): void {

    localStorage.removeItem(
      this.storageKey
    );
  }
}