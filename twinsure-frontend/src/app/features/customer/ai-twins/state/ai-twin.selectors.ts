// Exposes AI Twin feature state to list, detail, form, and permission pages.

import { createSelector } from '@ngrx/store';

import { aiTwinFeature } from './ai-twin.reducer';

export const {
  selectAiTwinsState,
  selectAiTwins,
  selectSelectedAiTwin,
  selectPermissions,
  selectLoading,
  selectSaving,
  selectPermissionsLoading,
  selectError,
} = aiTwinFeature;

export const selectActiveAiTwins = createSelector(selectAiTwins, (aiTwins) =>
  aiTwins.filter((aiTwin) => aiTwin.status === 'ACTIVE'),
);

export const selectHasAiTwins = createSelector(selectAiTwins, (aiTwins) => aiTwins.length > 0);
