// Handles AI Twin Service calls and emits typed success or failure actions.
// Components dispatch intentions without directly performing HTTP requests.

import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { Actions, createEffect, ofType } from '@ngrx/effects';

import { catchError, exhaustMap, map, of, switchMap, tap } from 'rxjs';

import { getApiErrorMessage } from '../../../../core/http/api-error.util';
import { AuthActions } from '../../../auth/state/auth.actions';
import { AiTwinApiService } from '../services/ai-twin-api.service';
import { AiTwinActions } from './ai-twin.actions';

@Injectable()
export class AiTwinEffects {
  private readonly actions$ = inject(Actions);

  private readonly aiTwinApi = inject(AiTwinApiService);

  private readonly router = inject(Router);

  readonly loadMyAiTwins$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AiTwinActions.loadMyAiTwins),

      switchMap(() =>
        this.aiTwinApi.getMyAiTwins().pipe(
          map((aiTwins) =>
            AiTwinActions.loadMyAiTwinsSuccess({
              aiTwins,
            }),
          ),

          catchError((error) =>
            of(
              AiTwinActions.loadMyAiTwinsFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly loadAiTwinDetails$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AiTwinActions.loadAiTwinDetails),

      switchMap(({ twinId }) =>
        this.aiTwinApi.getAiTwin(twinId).pipe(
          map((aiTwin) =>
            AiTwinActions.loadAiTwinDetailsSuccess({
              aiTwin,
            }),
          ),

          catchError((error) =>
            of(
              AiTwinActions.loadAiTwinDetailsFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly createAiTwin$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AiTwinActions.createAiTwin),

      exhaustMap(({ request }) =>
        this.aiTwinApi.createAiTwin(request).pipe(
          map((aiTwin) =>
            AiTwinActions.createAiTwinSuccess({
              aiTwin,
            }),
          ),

          catchError((error) =>
            of(
              AiTwinActions.createAiTwinFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly navigateAfterCreate$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AiTwinActions.createAiTwinSuccess),

        tap(({ aiTwin }) => this.router.navigate(['/customer/ai-twins', aiTwin.twinId])),
      ),
    {
      dispatch: false,
    },
  );

  readonly updateAiTwin$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AiTwinActions.updateAiTwin),

      exhaustMap(({ twinId, request }) =>
        this.aiTwinApi.updateAiTwin(twinId, request).pipe(
          map((aiTwin) =>
            AiTwinActions.updateAiTwinSuccess({
              aiTwin,
            }),
          ),

          catchError((error) =>
            of(
              AiTwinActions.updateAiTwinFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly loadPermissions$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AiTwinActions.loadPermissions),

      switchMap(({ twinId }) =>
        this.aiTwinApi.getPermissions(twinId).pipe(
          map((permissions) =>
            AiTwinActions.loadPermissionsSuccess({
              permissions,
            }),
          ),

          catchError((error) =>
            of(
              AiTwinActions.loadPermissionsFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly configurePermission$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AiTwinActions.configurePermission),

      exhaustMap(({ twinId, request }) =>
        this.aiTwinApi.configurePermission(twinId, request).pipe(
          map((permission) =>
            AiTwinActions.configurePermissionSuccess({
              permission,
            }),
          ),

          catchError((error) =>
            of(
              AiTwinActions.configurePermissionFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly clearOnLogout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.logoutSuccess),

      map(() => AiTwinActions.clearAiTwinState()),
    ),
  );
}
