// Stores notification history, unread count, loading state, and errors.
// The reducer remains synchronous and never performs HTTP operations.
// Backend data is reloaded after bulk updates to prevent stale UI state.

import {
  createFeature,
  createReducer,
  on
} from '@ngrx/store';

import {
  Notification
} from '../models/notification.model';
import {
  NotificationActions
} from './notification.actions';

export interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  loading: boolean;
  updating: boolean;
  error: string | null;
}

const initialState: NotificationState = {
  notifications: [],
  unreadCount: 0,
  loading: false,
  updating: false,
  error: null
};

export const notificationFeature =
  createFeature({
    name: 'notifications',

    reducer: createReducer(
      initialState,

      on(
        NotificationActions.loadNotifications,
        state => ({
          ...state,
          loading: true,
          error: null
        })
      ),

      on(
        NotificationActions.loadNotificationsSuccess,
        (
          state,
          {
            notifications
          }
        ) => ({
          ...state,
          notifications,
          loading: false,
          error: null
        })
      ),

      on(
        NotificationActions.loadNotificationsFailure,
        (
          state,
          {
            error
          }
        ) => ({
          ...state,
          loading: false,
          error
        })
      ),

      on(
        NotificationActions.loadUnreadCountSuccess,
        (
          state,
          {
            unreadCount
          }
        ) => ({
          ...state,
          unreadCount,
          error: null
        })
      ),

      on(
        NotificationActions.loadUnreadCountFailure,
        (
          state,
          {
            error
          }
        ) => ({
          ...state,
          error
        })
      ),

      on(
        NotificationActions.markAsRead,
        state => ({
          ...state,
          updating: true,
          error: null
        })
      ),

      on(
        NotificationActions.markAsReadSuccess,
        (
          state,
          {
            notification
          }
        ) => {

          const previousNotification =
            state.notifications.find(item =>
              item.notificationId
                === notification.notificationId
            );

          const unreadNotificationWasUpdated =
            previousNotification !== undefined
            && !previousNotification.readStatus;

          return {
            ...state,

            notifications:
              state.notifications.map(item =>
                item.notificationId
                  === notification.notificationId
                    ? notification
                    : item
              ),

            unreadCount:
              unreadNotificationWasUpdated
                ? Math.max(
                    0,
                    state.unreadCount - 1
                  )
                : state.unreadCount,

            updating: false,
            error: null
          };
        }
      ),

      on(
        NotificationActions.markAsReadFailure,
        (
          state,
          {
            error
          }
        ) => ({
          ...state,
          updating: false,
          error
        })
      ),

      on(
        NotificationActions.markAllAsRead,
        state => ({
          ...state,
          updating: true,
          error: null
        })
      ),

      on(
        NotificationActions.markAllAsReadSuccess,
        state => ({
          ...state,
          updating: false,
          error: null
        })
      ),

      on(
        NotificationActions.markAllAsReadFailure,
        (
          state,
          {
            error
          }
        ) => ({
          ...state,
          updating: false,
          error
        })
      ),

      on(
        NotificationActions.clearNotifications,
        () => ({
          ...initialState
        })
      ),

      on(
        NotificationActions.clearError,
        state => ({
          ...state,
          error: null
        })
      )
    )
  });