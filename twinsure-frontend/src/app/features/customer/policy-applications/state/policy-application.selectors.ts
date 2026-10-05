// Exposes focused policy-application state for Customer pages.

import { createSelector } from '@ngrx/store';

import { policyApplicationFeature } from './policy-application.reducer';

export const {
  selectPolicyApplicationsState,
  selectApplications,
  selectSelectedDetails,
  selectIssuedPolicy,
  selectLoading,
  selectSaving,
  selectError,
} = policyApplicationFeature;

export const selectPendingApplications = createSelector(selectApplications, (applications) =>
  applications.filter((application) => application.status === 'PENDING_REVIEW'),
);

export const selectApprovedApplications = createSelector(selectApplications, (applications) =>
  applications.filter((application) => application.status === 'APPROVED'),
);
