// Defines notification events for loading, reading, and clearing alerts.
// Effects handle backend calls, while the reducer updates local state.

import {
  createActionGroup,
  emptyProps,
  props
} from '@ngrx/store';

import {
  Notification
} from '../models/notification.model';

export const NotificationActions =
  createActionGroup({
    source: 'Notifications',

    events: {
      'Load Notifications': emptyProps(),

      'Load Notifications Success': props<{
        notifications: Notification[];
      }>(),

      'Load Notifications Failure': props<{
        error: string;
      }>(),

      'Load Unread Count': emptyProps(),

      'Load Unread Count Success': props<{
        unreadCount: number;
      }>(),

      'Load Unread Count Failure': props<{
        error: string;
      }>(),

      'Mark As Read': props<{
        notificationId: number;
      }>(),

      'Mark As Read Success': props<{
        notification: Notification;
      }>(),

      'Mark As Read Failure': props<{
        error: string;
      }>(),

      'Mark All As Read': emptyProps(),

      'Mark All As Read Success': props<{
        updatedCount: number;
      }>(),

      'Mark All As Read Failure': props<{
        error: string;
      }>(),

      'Clear Notifications': emptyProps(),

      'Clear Error': emptyProps()
    }
  });