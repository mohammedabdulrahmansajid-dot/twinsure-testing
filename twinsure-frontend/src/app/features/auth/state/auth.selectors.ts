// Exposes focused authentication state for components and route guards.
// Components consume selectors instead of reading the Store structure directly.

import { createSelector } from '@ngrx/store';

import { authFeature } from './auth.reducer';

export const {
  selectAuthState,
  selectUser,
  selectAuthenticated,
  selectRestoring,
  selectLoading,
  selectError,
  selectRegistrationResponse,
} = authFeature;

export const selectUserRole = createSelector(selectUser, (user) => user?.role ?? null);

export const selectCustomerId = createSelector(selectUser, (user) => user?.customerId ?? null);

export const selectUsername = createSelector(selectUser, (user) => user?.username ?? null);
