// Stores the Adjuster queue, selected claim, and review context.
// Core claim errors and review-context errors are kept separate.

import { createFeature, createReducer, on } from '@ngrx/store';

import { ClaimDetails } from '../models/claim-details.model';
import { ClaimResponse } from '../models/claim-response.model';
import { ClaimReviewContext } from '../models/claim-review-context.model';
import { ClaimStatus } from '../models/claim-types.model';
import { ClaimsAdjusterActions } from './claims-adjuster.actions';

export interface ClaimsAdjusterState {
  assignedClaims: ClaimResponse[];
  selectedDetails: ClaimDetails | null;
  reviewContext: ClaimReviewContext | null;
  loading: boolean;
  contextLoading: boolean;
  saving: boolean;
  error: string | null;
  contextError: string | null;
}

const initialState: ClaimsAdjusterState = {
  assignedClaims: [],
  selectedDetails: null,
  reviewContext: null,
  loading: false,
  contextLoading: false,
  saving: false,
  error: null,
  contextError: null,
};

// const statusPriority: Record<ClaimStatus, number> = {
//   ASSIGNED: 1,
//   MORE_INFORMATION_REQUIRED: 2,
//   UNDER_REVIEW: 3,
//   APPROVED: 4,
//   PARTIALLY_APPROVED: 5,
//   REJECTED: 6,
//   CLOSED: 7,
//   SUBMITTED: 8,
// };

function sortAssignedClaims(claims: ClaimResponse[]): ClaimResponse[] {
  return [...claims].sort(
    (first, second) => new Date(second.updatedAt).getTime() - new Date(first.updatedAt).getTime(),
  );
}

export const claimsAdjusterFeature = createFeature({
  name: 'claimsAdjuster',

  reducer: createReducer(
    initialState,

    on(ClaimsAdjusterActions.loadAssignedClaims, (state) => ({
      ...state,
      loading: true,
      error: null,
    })),

    on(ClaimsAdjusterActions.loadAssignedClaimsSuccess, (state, { claims }) => ({
      ...state,
      assignedClaims: sortAssignedClaims(claims),
      loading: false,
      error: null,
    })),

    on(ClaimsAdjusterActions.loadAssignedClaimsFailure, (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })),

    on(ClaimsAdjusterActions.loadClaimDetails, (state) => ({
      ...state,
      selectedDetails: null,
      loading: true,
      error: null,
    })),

    on(ClaimsAdjusterActions.loadClaimDetailsSuccess, (state, { details }) => ({
      ...state,

      selectedDetails: {
        ...details,

        documents: [...details.documents].sort(
          (first, second) =>
            new Date(second.uploadedAt).getTime() - new Date(first.uploadedAt).getTime(),
        ),

        decisions: [...details.decisions].sort(
          (first, second) =>
            new Date(second.decidedAt).getTime() - new Date(first.decidedAt).getTime(),
        ),
      },

      loading: false,
      error: null,
    })),

    on(ClaimsAdjusterActions.loadClaimDetailsFailure, (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })),

    on(ClaimsAdjusterActions.loadReviewContext, (state) => ({
      ...state,
      reviewContext: null,
      contextLoading: true,
      contextError: null,
    })),

    on(ClaimsAdjusterActions.loadReviewContextSuccess, (state, { reviewContext }) => ({
      ...state,
      reviewContext,
      contextLoading: false,
      contextError: null,
    })),

    on(ClaimsAdjusterActions.loadReviewContextFailure, (state, { error }) => ({
      ...state,
      contextLoading: false,
      contextError: error,
    })),

    on(
      ClaimsAdjusterActions.startReview,
      ClaimsAdjusterActions.requestInformation,
      ClaimsAdjusterActions.approveClaim,
      ClaimsAdjusterActions.partiallyApproveClaim,
      ClaimsAdjusterActions.rejectClaim,
      ClaimsAdjusterActions.closeClaim,
      (state) => ({
        ...state,
        saving: true,
        error: null,
      }),
    ),

    on(ClaimsAdjusterActions.workflowOperationSuccess, (state) => ({
      ...state,
      saving: false,
      error: null,
    })),

    on(ClaimsAdjusterActions.workflowOperationFailure, (state, { error }) => ({
      ...state,
      saving: false,
      error,
    })),

    on(ClaimsAdjusterActions.clearSelectedClaim, (state) => ({
      ...state,
      selectedDetails: null,
      reviewContext: null,
      contextLoading: false,
      saving: false,
      error: null,
      contextError: null,
    })),

    on(ClaimsAdjusterActions.clearClaimsAdjusterState, () => ({
      ...initialState,
    })),

    on(ClaimsAdjusterActions.clearError, (state) => ({
      ...state,
      error: null,
    })),
  ),
});
