// Handles Notification Service API calls and converts responses
// into success or failure actions for the notification reducer.
// Successful bulk updates reload the backend state to prevent stale UI data.

import {
  inject,
  Injectable
} from '@angular/core';

import {
  Actions,
  createEffect,
  ofType
} from '@ngrx/effects';

import {
  catchError,
  exhaustMap,
  map,
  of,
  switchMap,
  timeout
} from 'rxjs';

import {
  getApiErrorMessage
} from '../../../core/http/api-error.util';
import {
  AuthActions
} from '../../auth/state/auth.actions';
import {
  NotificationApiService
} from '../services/notification-api.service';
import {
  NotificationActions
} from './notification.actions';

@Injectable()
export class NotificationEffects {

  private readonly actions$ =
    inject(Actions);

  private readonly notificationApi =
    inject(NotificationApiService);

  readonly loadNotifications$ =
    createEffect(() =>
      this.actions$.pipe(
        ofType(
          NotificationActions.loadNotifications
        ),

        switchMap(() =>
          this.notificationApi
            .getMyNotifications()
            .pipe(
              map(notifications =>
                NotificationActions
                  .loadNotificationsSuccess({
                    notifications
                  })
              ),

              catchError(error =>
                of(
                  NotificationActions
                    .loadNotificationsFailure({
                      error:
                        getApiErrorMessage(error)
                    })
                )
              )
            )
        )
      )
    );

  readonly loadUnreadCount$ =
    createEffect(() =>
      this.actions$.pipe(
        ofType(
          NotificationActions.loadUnreadCount
        ),

        switchMap(() =>
          this.notificationApi
            .getUnreadCount()
            .pipe(
              map(response =>
                NotificationActions
                  .loadUnreadCountSuccess({
                    unreadCount:
                      response.unreadCount
                  })
              ),

              catchError(error =>
                of(
                  NotificationActions
                    .loadUnreadCountFailure({
                      error:
                        getApiErrorMessage(error)
                    })
                )
              )
            )
        )
      )
    );

  readonly markAsRead$ =
    createEffect(() =>
      this.actions$.pipe(
        ofType(
          NotificationActions.markAsRead
        ),

        exhaustMap(
          ({
            notificationId
          }) =>
            this.notificationApi
              .markAsRead(notificationId)
              .pipe(
                timeout(10000),

                map(notification =>
                  NotificationActions
                    .markAsReadSuccess({
                      notification
                    })
                ),

                catchError(error =>
                  of(
                    NotificationActions
                      .markAsReadFailure({
                        error:
                          error.name
                            === 'TimeoutError'
                            ? 'The notification update took too long. Please try again.'
                            : getApiErrorMessage(
                                error
                              )
                      })
                  )
                )
              )
        )
      )
    );

  readonly markAllAsRead$ =
    createEffect(() =>
      this.actions$.pipe(
        ofType(
          NotificationActions.markAllAsRead
        ),

        exhaustMap(() =>
          this.notificationApi
            .markAllAsRead()
            .pipe(
              timeout(10000),

              map(response =>
                NotificationActions
                  .markAllAsReadSuccess({
                    updatedCount:
                      response.updatedCount
                  })
              ),

              catchError(error =>
                of(
                  NotificationActions
                    .markAllAsReadFailure({
                      error:
                        error.name
                          === 'TimeoutError'
                          ? 'The notification update took too long. Please try again.'
                          : getApiErrorMessage(
                              error
                            )
                    })
                )
              )
            )
        )
      )
    );

  readonly reloadAfterMarkAll$ =
    createEffect(() =>
      this.actions$.pipe(
        ofType(
          NotificationActions.markAllAsReadSuccess
        ),

        switchMap(() => [
          NotificationActions
            .loadNotifications(),

          NotificationActions
            .loadUnreadCount()
        ])
      )
    );

  readonly clearOnLogout$ =
    createEffect(() =>
      this.actions$.pipe(
        ofType(
          AuthActions.logoutSuccess
        ),

        map(() =>
          NotificationActions
            .clearNotifications()
        )
      )
    );
}