// Exposes the queue, selected claim, and review context.
// Derived counts and queue groups are calculated from assigned claims.

import { createSelector } from '@ngrx/store';

import { claimsAdjusterFeature } from './claims-adjuster.reducer';

export const {
  selectClaimsAdjusterState,
  selectAssignedClaims,
  selectSelectedDetails,
  selectReviewContext,
  selectLoading,
  selectContextLoading,
  selectSaving,
  selectError,
  selectContextError,
} = claimsAdjusterFeature;

export const selectAssignedCount = createSelector(
  selectAssignedClaims,
  (claims) => claims.filter((claim) => claim.status === 'ASSIGNED').length,
);

export const selectUnderReviewCount = createSelector(
  selectAssignedClaims,
  (claims) => claims.filter((claim) => claim.status === 'UNDER_REVIEW').length,
);

export const selectInformationRequiredCount = createSelector(
  selectAssignedClaims,
  (claims) => claims.filter((claim) => claim.status === 'MORE_INFORMATION_REQUIRED').length,
);

export const selectCompletedCount = createSelector(
  selectAssignedClaims,
  (claims) =>
    claims.filter(
      (claim) =>
        claim.status === 'APPROVED' ||
        claim.status === 'PARTIALLY_APPROVED' ||
        claim.status === 'REJECTED' ||
        claim.status === 'CLOSED',
    ).length,
);

export const selectActiveClaims = createSelector(selectAssignedClaims, (claims) =>
  claims.filter(
    (claim) =>
      claim.status === 'ASSIGNED' ||
      claim.status === 'UNDER_REVIEW' ||
      claim.status === 'MORE_INFORMATION_REQUIRED',
  ),
);

export const selectCompletedClaims = createSelector(selectAssignedClaims, (claims) =>
  claims.filter(
    (claim) =>
      claim.status === 'APPROVED' ||
      claim.status === 'PARTIALLY_APPROVED' ||
      claim.status === 'REJECTED' ||
      claim.status === 'CLOSED',
  ),
);
