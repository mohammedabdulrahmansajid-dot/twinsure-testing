// Exposes focused notification state for layouts and feature pages.
// Components use selectors instead of reading Store structure directly.

import { createSelector } from '@ngrx/store';

import { notificationFeature } from './notification.reducer';

export const {
  selectNotificationsState,
  selectNotifications,
  selectUnreadCount,
  selectLoading,
  selectUpdating,
  selectError,
} = notificationFeature;

export const selectUnreadNotifications = createSelector(selectNotifications, (notifications) =>
  notifications.filter((notification) => !notification.readStatus),
);

export const selectHasUnreadNotifications = createSelector(
  selectUnreadCount,
  (unreadCount) => unreadCount > 0,
);
