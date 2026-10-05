// Displays notifications owned by the authenticated TwinSure user.
// The component dispatches user intentions while NgRx Effects perform
// backend operations and the Store provides reactive page state.

import { ChangeDetectionStrategy, Component, computed, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';

import { Store } from '@ngrx/store';

import { NotificationActions } from '../../state/notification.actions';
import {
  selectError,
  selectLoading,
  selectNotifications,
  selectUnreadCount,
  selectUpdating,
} from '../../state/notification.selectors';

@Component({
  selector: 'app-notification-list',
  imports: [DatePipe],
  templateUrl: './notification-list.html',
  styleUrl: './notification-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationList implements OnInit {
  private readonly store = inject(Store);

  readonly notifications = this.store.selectSignal(selectNotifications);

  readonly unreadCount = this.store.selectSignal(selectUnreadCount);

  readonly loading = this.store.selectSignal(selectLoading);

  readonly updating = this.store.selectSignal(selectUpdating);

  readonly error = this.store.selectSignal(selectError);

  readonly readCount = computed(
    () => this.notifications().filter((notification) => notification.readStatus).length,
  );

  readonly loadingItems = [1, 2, 3];

  ngOnInit(): void {
    this.store.dispatch(NotificationActions.loadNotifications());

    this.store.dispatch(NotificationActions.loadUnreadCount());
  }

  markAsRead(notificationId: number): void {
    this.store.dispatch(
      NotificationActions.markAsRead({
        notificationId,
      }),
    );
  }

  markAllAsRead(): void {
    this.store.dispatch(NotificationActions.markAllAsRead());
  }
}
