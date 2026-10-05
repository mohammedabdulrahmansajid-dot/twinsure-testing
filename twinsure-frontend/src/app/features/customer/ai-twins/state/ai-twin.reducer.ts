// Stores Customer-owned AI Twins, the selected Twin, permissions,
// loading indicators, mutation state, and backend errors.

import { createFeature, createReducer, on } from '@ngrx/store';

import { AiTwinResponse } from '../models/ai-twin-response.model';
import { TwinPermissionResponse } from '../models/twin-permission-response.model';
import { AiTwinActions } from './ai-twin.actions';

export interface AiTwinState {
  aiTwins: AiTwinResponse[];
  selectedAiTwin: AiTwinResponse | null;
  permissions: TwinPermissionResponse[];
  loading: boolean;
  saving: boolean;
  permissionsLoading: boolean;
  error: string | null;
}

const initialState: AiTwinState = {
  aiTwins: [],
  selectedAiTwin: null,
  permissions: [],
  loading: false,
  saving: false,
  permissionsLoading: false,
  error: null,
};

export const aiTwinFeature = createFeature({
  name: 'aiTwins',

  reducer: createReducer(
    initialState,

    on(AiTwinActions.loadMyAiTwins, (state) => ({
      ...state,
      loading: true,
      error: null,
    })),

    on(AiTwinActions.loadMyAiTwinsSuccess, (state, { aiTwins }) => ({
      ...state,
      aiTwins,
      loading: false,
      error: null,
    })),

    on(AiTwinActions.loadMyAiTwinsFailure, (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })),

    on(AiTwinActions.loadAiTwinDetails, (state) => ({
      ...state,
      loading: true,
      selectedAiTwin: null,
      permissions: [],
      error: null,
    })),

    on(AiTwinActions.loadAiTwinDetailsSuccess, (state, { aiTwin }) => ({
      ...state,
      selectedAiTwin: aiTwin,
      loading: false,
      error: null,
    })),

    on(AiTwinActions.loadAiTwinDetailsFailure, (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })),

    on(AiTwinActions.createAiTwin, AiTwinActions.updateAiTwin, (state) => ({
      ...state,
      saving: true,
      error: null,
    })),

    on(AiTwinActions.createAiTwinSuccess, (state, { aiTwin }) => ({
      ...state,
      aiTwins: [aiTwin, ...state.aiTwins],
      selectedAiTwin: aiTwin,
      saving: false,
      error: null,
    })),

    on(AiTwinActions.updateAiTwinSuccess, (state, { aiTwin }) => ({
      ...state,
      aiTwins: state.aiTwins.map((item) => (item.twinId === aiTwin.twinId ? aiTwin : item)),
      selectedAiTwin: aiTwin,
      saving: false,
      error: null,
    })),

    on(
      AiTwinActions.createAiTwinFailure,
      AiTwinActions.updateAiTwinFailure,
      (state, { error }) => ({
        ...state,
        saving: false,
        error,
      }),
    ),

    on(AiTwinActions.loadPermissions, (state) => ({
      ...state,
      permissionsLoading: true,
      error: null,
    })),

    on(AiTwinActions.loadPermissionsSuccess, (state, { permissions }) => ({
      ...state,
      permissions,
      permissionsLoading: false,
      error: null,
    })),

    on(AiTwinActions.loadPermissionsFailure, (state, { error }) => ({
      ...state,
      permissionsLoading: false,
      error,
    })),

    on(AiTwinActions.configurePermission, (state) => ({
      ...state,
      saving: true,
      error: null,
    })),

    on(AiTwinActions.configurePermissionSuccess, (state, { permission }) => {
      const matchingPermissionExists = state.permissions.some(
        (item) => item.actionType === permission.actionType,
      );

      return {
        ...state,

        permissions: matchingPermissionExists
          ? state.permissions.map((item) =>
              item.actionType === permission.actionType ? permission : item,
            )
          : [...state.permissions, permission],

        saving: false,
        error: null,
      };
    }),

    on(AiTwinActions.configurePermissionFailure, (state, { error }) => ({
      ...state,
      saving: false,
      error,
    })),

    on(AiTwinActions.clearSelectedAiTwin, (state) => ({
      ...state,
      selectedAiTwin: null,
      permissions: [],
      error: null,
    })),

    on(AiTwinActions.clearAiTwinState, () => ({
      ...initialState,
    })),

    on(AiTwinActions.clearError, (state) => ({
      ...state,
      error: null,
    })),
  ),
});
