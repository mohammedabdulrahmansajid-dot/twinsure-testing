// Handles Customer policy-application HTTP operations and navigation.
// Mutation effects use exhaustMap to prevent duplicate submissions.

import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { Actions, createEffect, ofType } from '@ngrx/effects';

import { catchError, exhaustMap, map, of, switchMap, tap } from 'rxjs';

import { getApiErrorMessage } from '../../../../core/http/api-error.util';
import { AuthActions } from '../../../auth/state/auth.actions';
import { PolicyApplicationApiService } from '../services/policy-application-api.service';
import { PolicyApplicationActions } from './policy-application.actions';

@Injectable()
export class PolicyApplicationEffects {
  private readonly actions$ = inject(Actions);

  private readonly applicationApi = inject(PolicyApplicationApiService);

  private readonly router = inject(Router);

  readonly loadMyApplications$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PolicyApplicationActions.loadMyApplications),

      switchMap(() =>
        this.applicationApi.getMyApplications().pipe(
          map((applications) =>
            PolicyApplicationActions.loadMyApplicationsSuccess({
              applications,
            }),
          ),

          catchError((error) =>
            of(
              PolicyApplicationActions.loadMyApplicationsFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly loadApplicationDetails$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PolicyApplicationActions.loadApplicationDetails),

      switchMap(({ applicationId }) =>
        this.applicationApi.getApplicationDetails(applicationId).pipe(
          map((details) =>
            PolicyApplicationActions.loadApplicationDetailsSuccess({
              details,
            }),
          ),

          catchError((error) =>
            of(
              PolicyApplicationActions.loadApplicationDetailsFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly createApplication$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PolicyApplicationActions.createApplication),

      exhaustMap(({ twinId, productId }) =>
        this.applicationApi
          .createApplication({
            twinId,
            productId,
          })
          .pipe(
            map((application) =>
              PolicyApplicationActions.createApplicationSuccess({
                application,
              }),
            ),

            catchError((error) =>
              of(
                PolicyApplicationActions.createApplicationFailure({
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
        ofType(PolicyApplicationActions.createApplicationSuccess),

        tap(({ application }) =>
          this.router.navigate(['/customer/policy-applications', application.applicationId]),
        ),
      ),
    {
      dispatch: false,
    },
  );

  readonly acceptApplication$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PolicyApplicationActions.acceptApplication),

      exhaustMap(({ applicationId }) =>
        this.applicationApi.acceptApplication(applicationId).pipe(
          map((policy) =>
            PolicyApplicationActions.acceptApplicationSuccess({
              policy,
            }),
          ),

          catchError((error) =>
            of(
              PolicyApplicationActions.acceptApplicationFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly navigateAfterAcceptance$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(PolicyApplicationActions.acceptApplicationSuccess),

        tap(({ policy }) => this.router.navigate(['/customer/policies', policy.policyId])),
      ),
    {
      dispatch: false,
    },
  );

  readonly declineApplication$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PolicyApplicationActions.declineApplication),

      exhaustMap(({ applicationId }) =>
        this.applicationApi.declineApplication(applicationId).pipe(
          map((application) =>
            PolicyApplicationActions.declineApplicationSuccess({
              application,
            }),
          ),

          catchError((error) =>
            of(
              PolicyApplicationActions.declineApplicationFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly resubmitApplication$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PolicyApplicationActions.resubmitApplication),

      exhaustMap(({ applicationId }) =>
        this.applicationApi.resubmitApplication(applicationId).pipe(
          map((application) =>
            PolicyApplicationActions.resubmitApplicationSuccess({
              application,
            }),
          ),

          catchError((error) =>
            of(
              PolicyApplicationActions.resubmitApplicationFailure({
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

      map(() => PolicyApplicationActions.clearPolicyApplicationState()),
    ),
  );
}
