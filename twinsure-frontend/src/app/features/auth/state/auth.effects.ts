// Handles authentication side effects such as API calls, navigation,
// and safe session persistence. Reducers remain synchronous and pure.

import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';

import {
  Actions,
  createEffect,
  ofType
} from '@ngrx/effects';
import {
  getDashboardRoute
} from '../../../core/constants/dashboard-route.util';
import {
  catchError,
  exhaustMap,
  map,
  of,
  tap
} from 'rxjs';

import {
  AuthenticatedUser
} from '../../../core/models/authenticated-user.model';
import {
  getApiErrorMessage
} from '../../../core/http/api-error.util';
import {
  AuthApiService
} from '../services/auth-api.service';
import {
  AuthSessionService
} from '../services/auth-session.service';
import {
  LoginResponse
} from '../models/login-response.dto';
import {
  AuthActions
} from './auth.actions';

@Injectable()
export class AuthEffects {

  private readonly actions$ = inject(Actions);
  private readonly authApi = inject(AuthApiService);
  private readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);

  readonly restoreSession$ =
    createEffect(() =>
      this.actions$.pipe(
        ofType(
          AuthActions.restoreSession
        ),
        map(() => this.session.loadUser()),
        map(user =>
          user === null
            ? AuthActions.restoreSessionEmpty()
            : AuthActions.restoreSessionSuccess({
                user
              })
        )
      )
    );

  readonly login$ =
    createEffect(() =>
      this.actions$.pipe(
        ofType(
          AuthActions.login
        ),
        exhaustMap(({ request }) =>
          this.authApi.login(request).pipe(
            map(response =>
              AuthActions.loginSuccess({
                user: this.mapLoginResponse(
                  response
                )
              })
            ),
            catchError(error =>
              of(
                AuthActions.loginFailure({
                  error: getApiErrorMessage(error)
                })
              )
            )
          )
        )
      )
    );

  readonly persistLogin$ =
    createEffect(
      () =>
        this.actions$.pipe(
          ofType(
            AuthActions.loginSuccess
          ),
          tap(({ user }) =>
            this.session.saveUser(user)
          ),
          tap(({ user }) =>
            this.router.navigateByUrl(
             getDashboardRoute(
                user.role
              )
            )
          )
        ),
      {
        dispatch: false
      }
    );

  readonly register$ =
    createEffect(() =>
      this.actions$.pipe(
        ofType(
          AuthActions.register
        ),
        exhaustMap(({ request }) =>
          this.authApi.register(request).pipe(
            map(response =>
              AuthActions.registerSuccess({
                response
              })
            ),
            catchError(error =>
              of(
                AuthActions.registerFailure({
                  error: getApiErrorMessage(error)
                })
              )
            )
          )
        )
      )
    );

  readonly registrationNavigation$ =
    createEffect(
      () =>
        this.actions$.pipe(
          ofType(
            AuthActions.registerSuccess
          ),
          tap(() =>
            this.router.navigate(
              ['/login'],
              {
                queryParams: {
                  registered: 'true'
                }
              }
            )
          )
        ),
      {
        dispatch: false
      }
    );

readonly logout$ =
  createEffect(() =>
    this.actions$.pipe(
      ofType(
        AuthActions.logout
      ),
      exhaustMap(() =>
        this.authApi.logout().pipe(
          map(() =>
            AuthActions.logoutSuccess()
          ),
          catchError(() =>
            of(
              AuthActions.logoutSuccess()
            )
          )
        )
      )
    )
  );
  readonly clearLogoutSession$ =
  createEffect(
    () =>
      this.actions$.pipe(
        ofType(
          AuthActions.logoutSuccess
        ),
        tap(() =>
          this.session.clear()
        ),
        tap(() =>
          this.router.navigateByUrl(
            '/login'
          )
        )
      ),
    {
      dispatch: false
    }
  );

  private mapLoginResponse(
    response: LoginResponse
  ): AuthenticatedUser {

    return {
      userId: response.userId,
      customerId: response.customerId,
      username: response.username,
      role: response.role
    };
  }


}