// Stores Customer policy applications, selected details,
// an accepted policy, loading states, and backend errors.

import { createFeature, createReducer, on } from '@ngrx/store';

import { PolicyApplicationDetails } from '../models/policy-application-details.model';
import { PolicyApplicationResponse } from '../models/policy-application-response.model';
import { PolicyResponse } from '../models/policy-response.model';
import { PolicyApplicationActions } from './policy-application.actions';

export interface PolicyApplicationState {
  applications: PolicyApplicationResponse[];

  selectedDetails: PolicyApplicationDetails | null;

  issuedPolicy: PolicyResponse | null;

  loading: boolean;

  saving: boolean;

  error: string | null;
}

const initialState: PolicyApplicationState = {
  applications: [],
  selectedDetails: null,
  issuedPolicy: null,
  loading: false,
  saving: false,
  error: null,
};

export const policyApplicationFeature = createFeature({
  name: 'policyApplications',

  reducer: createReducer(
    initialState,

    on(PolicyApplicationActions.loadMyApplications, (state) => ({
      ...state,
      loading: true,
      error: null,
    })),

    on(PolicyApplicationActions.loadMyApplicationsSuccess, (state, { applications }) => ({
      ...state,

      applications: [...applications].sort(
        (first, second) =>
          new Date(second.submittedAt).getTime() - new Date(first.submittedAt).getTime(),
      ),

      loading: false,
      error: null,
    })),

    on(PolicyApplicationActions.loadMyApplicationsFailure, (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })),

    on(PolicyApplicationActions.loadApplicationDetails, (state) => ({
      ...state,
      selectedDetails: null,
      issuedPolicy: null,
      loading: true,
      error: null,
    })),

    on(PolicyApplicationActions.loadApplicationDetailsSuccess, (state, { details }) => ({
      ...state,
      selectedDetails: details,
      loading: false,
      error: null,
    })),

    on(PolicyApplicationActions.loadApplicationDetailsFailure, (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })),

    on(
      PolicyApplicationActions.createApplication,
      PolicyApplicationActions.acceptApplication,
      PolicyApplicationActions.declineApplication,
      PolicyApplicationActions.resubmitApplication,
      (state) => ({
        ...state,
        saving: true,
        error: null,
      }),
    ),

    on(PolicyApplicationActions.createApplicationSuccess, (state, { application }) => ({
      ...state,

      applications: [application, ...state.applications],

      saving: false,
      error: null,
    })),

    on(PolicyApplicationActions.acceptApplicationSuccess, (state, { policy }) => {
      const acceptedAt = new Date().toISOString();

      const selectedApplication = state.selectedDetails?.application;

      const updatedApplication: PolicyApplicationResponse | null =
        selectedApplication === undefined
          ? null
          : {
              ...selectedApplication,
              status: 'ACCEPTED',
              acceptedAt,
            };

      return {
        ...state,

        applications: state.applications.map((application) =>
          application.applicationId === policy.applicationId
            ? {
                ...application,
                status: 'ACCEPTED' as const,
                acceptedAt,
              }
            : application,
        ),

        selectedDetails:
          state.selectedDetails === null || updatedApplication === null
            ? state.selectedDetails
            : {
                ...state.selectedDetails,
                application: updatedApplication,
              },

        issuedPolicy: policy,

        saving: false,

        error: null,
      };
    }),

    on(PolicyApplicationActions.declineApplicationSuccess, (state, { application }) => ({
      ...state,

      applications: state.applications.map((item) =>
        item.applicationId === application.applicationId ? application : item,
      ),

      selectedDetails:
        state.selectedDetails === null
          ? null
          : {
              ...state.selectedDetails,
              application,
            },

      issuedPolicy: null,

      saving: false,

      error: null,
    })),

    on(PolicyApplicationActions.resubmitApplicationSuccess, (state, { application }) => ({
      ...state,

      applications: state.applications.map((item) =>
        item.applicationId === application.applicationId ? application : item,
      ),

      selectedDetails:
        state.selectedDetails === null
          ? null
          : {
              ...state.selectedDetails,
              application,
            },

      issuedPolicy: null,

      saving: false,

      error: null,
    })),

    on(
      PolicyApplicationActions.createApplicationFailure,
      PolicyApplicationActions.acceptApplicationFailure,
      PolicyApplicationActions.declineApplicationFailure,
      PolicyApplicationActions.resubmitApplicationFailure,
      (state, { error }) => ({
        ...state,
        saving: false,
        error,
      }),
    ),

    on(PolicyApplicationActions.clearSelectedApplication, (state) => ({
      ...state,
      selectedDetails: null,
      issuedPolicy: null,
      error: null,
    })),

    on(PolicyApplicationActions.clearPolicyApplicationState, () => ({
      ...initialState,
    })),

    on(PolicyApplicationActions.clearError, (state) => ({
      ...state,
      error: null,
    })),
  ),
});
