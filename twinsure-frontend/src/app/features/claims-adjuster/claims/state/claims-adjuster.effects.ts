// Handles queue, claim-details, review-context, and workflow requests.
// Successful mutations reload authoritative claim and financial data.

import { inject, Injectable } from '@angular/core';

import { Actions, createEffect, ofType } from '@ngrx/effects';

import { catchError, exhaustMap, map, of, switchMap } from 'rxjs';

import { getApiErrorMessage } from '../../../../core/http/api-error.util';
import { AuthActions } from '../../../auth/state/auth.actions';
import { ClaimsAdjusterApiService } from '../services/claims-adjuster-api.service';
import { ClaimsAdjusterActions } from './claims-adjuster.actions';

@Injectable()
export class ClaimsAdjusterEffects {
  private readonly actions$ = inject(Actions);

  private readonly claimsApi = inject(ClaimsAdjusterApiService);

  readonly loadAssignedClaims$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.loadAssignedClaims),

      switchMap(() =>
        this.claimsApi.getMyAssignedClaims().pipe(
          map((claims) =>
            ClaimsAdjusterActions.loadAssignedClaimsSuccess({
              claims,
            }),
          ),

          catchError((error) =>
            of(
              ClaimsAdjusterActions.loadAssignedClaimsFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly loadClaimDetails$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.loadClaimDetails),

      switchMap(({ claimId }) =>
        this.claimsApi.getClaimDetails(claimId).pipe(
          map((details) =>
            ClaimsAdjusterActions.loadClaimDetailsSuccess({
              details,
            }),
          ),

          catchError((error) =>
            of(
              ClaimsAdjusterActions.loadClaimDetailsFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly loadReviewContext$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.loadReviewContext),

      switchMap(({ claimId }) =>
        this.claimsApi.getReviewContext(claimId).pipe(
          map((reviewContext) =>
            ClaimsAdjusterActions.loadReviewContextSuccess({
              reviewContext,
            }),
          ),

          catchError((error) =>
            of(
              ClaimsAdjusterActions.loadReviewContextFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly startReview$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.startReview),

      exhaustMap(({ claimId, request }) =>
        this.claimsApi.startReview(claimId, request).pipe(
          map(() =>
            ClaimsAdjusterActions.workflowOperationSuccess({
              claimId,
            }),
          ),

          catchError((error) =>
            of(
              ClaimsAdjusterActions.workflowOperationFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly requestInformation$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.requestInformation),

      exhaustMap(({ claimId, request }) =>
        this.claimsApi.requestInformation(claimId, request).pipe(
          map(() =>
            ClaimsAdjusterActions.workflowOperationSuccess({
              claimId,
            }),
          ),

          catchError((error) =>
            of(
              ClaimsAdjusterActions.workflowOperationFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly approveClaim$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.approveClaim),

      exhaustMap(({ claimId, request }) =>
        this.claimsApi.approveClaim(claimId, request).pipe(
          map(() =>
            ClaimsAdjusterActions.workflowOperationSuccess({
              claimId,
            }),
          ),

          catchError((error) =>
            of(
              ClaimsAdjusterActions.workflowOperationFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly partiallyApproveClaim$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.partiallyApproveClaim),

      exhaustMap(({ claimId, request }) =>
        this.claimsApi.partiallyApproveClaim(claimId, request).pipe(
          map(() =>
            ClaimsAdjusterActions.workflowOperationSuccess({
              claimId,
            }),
          ),

          catchError((error) =>
            of(
              ClaimsAdjusterActions.workflowOperationFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly rejectClaim$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.rejectClaim),

      exhaustMap(({ claimId, request }) =>
        this.claimsApi.rejectClaim(claimId, request).pipe(
          map(() =>
            ClaimsAdjusterActions.workflowOperationSuccess({
              claimId,
            }),
          ),

          catchError((error) =>
            of(
              ClaimsAdjusterActions.workflowOperationFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly closeClaim$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.closeClaim),

      exhaustMap(({ claimId, request }) =>
        this.claimsApi.closeClaim(claimId, request).pipe(
          map(() =>
            ClaimsAdjusterActions.workflowOperationSuccess({
              claimId,
            }),
          ),

          catchError((error) =>
            of(
              ClaimsAdjusterActions.workflowOperationFailure({
                error: getApiErrorMessage(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  readonly reloadAfterWorkflow$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClaimsAdjusterActions.workflowOperationSuccess),

      switchMap(({ claimId }) => [
        ClaimsAdjusterActions.loadClaimDetails({
          claimId,
        }),

        ClaimsAdjusterActions.loadReviewContext({
          claimId,
        }),

        ClaimsAdjusterActions.loadAssignedClaims(),
      ]),
    ),
  );

  readonly clearOnLogout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.logoutSuccess),

      map(() => ClaimsAdjusterActions.clearClaimsAdjusterState()),
    ),
  );
}
