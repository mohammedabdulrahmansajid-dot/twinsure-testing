// Stores safe authentication state for the Angular application.
// The JWT is intentionally absent because authentication uses
// the HttpOnly TWINSURE_TOKEN cookie.

import {
  createFeature,
  createReducer,
  on
} from '@ngrx/store';

import {
  AuthenticatedUser
} from '../../../core/models/authenticated-user.model';
import {
  RegistrationResponse
} from '../models/registration-response.dto';
import {
  AuthActions
} from './auth.actions';

export interface AuthState {
  user: AuthenticatedUser | null;
  authenticated: boolean;
  restoring: boolean;
  loading: boolean;
  error: string | null;
  registrationResponse: RegistrationResponse | null;
}

const initialState: AuthState = {
  user: null,
  authenticated: false,
  restoring: true,
  loading: false,
  error: null,
  registrationResponse: null
};

export const authFeature =
  createFeature({
    name: 'auth',

    reducer: createReducer(
      initialState,

      on(
        AuthActions.restoreSession,
        state => ({
          ...state,
          restoring: true,
          error: null
        })
      ),

      on(
        AuthActions.restoreSessionSuccess,
        (state, { user }) => ({
          ...state,
          user,
          authenticated: true,
          restoring: false
        })
      ),

      on(
        AuthActions.restoreSessionEmpty,
        state => ({
          ...state,
          user: null,
          authenticated: false,
          restoring: false
        })
      ),

      on(
        AuthActions.login,
        state => ({
          ...state,
          loading: true,
          error: null,
          registrationResponse: null
        })
      ),

      on(
        AuthActions.loginSuccess,
        (state, { user }) => ({
          ...state,
          user,
          authenticated: true,
          loading: false,
          error: null
        })
      ),

      on(
        AuthActions.loginFailure,
        (state, { error }) => ({
          ...state,
          user: null,
          authenticated: false,
          loading: false,
          error
        })
      ),

      on(
        AuthActions.register,
        state => ({
          ...state,
          loading: true,
          error: null,
          registrationResponse: null
        })
      ),

      on(
        AuthActions.registerSuccess,
        (state, { response }) => ({
          ...state,
          loading: false,
          error: null,
          registrationResponse: response
        })
      ),

      on(
        AuthActions.registerFailure,
        (state, { error }) => ({
          ...state,
          loading: false,
          error
        })
      ),

      on(
        AuthActions.logout,
        state => ({
          ...state,
          loading: true,
          error: null
        })
      ),

      on(
        AuthActions.logoutSuccess,
        state => ({
          ...initialState,
          restoring: false
        })
      ),


      on(
        AuthActions.clearError,
        state => ({
          ...state,
          error: null
        })
      )
    )
  });