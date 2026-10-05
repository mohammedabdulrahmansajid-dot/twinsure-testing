// Provides typed Notification Service requests through the API Gateway.
// Cookie credentials are added centrally by the credentials interceptor.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { MarkAllReadResponse } from '../models/mark-all-read-response.model';
import { Notification } from '../models/notification.model';
import { UnreadCountResponse } from '../models/unread-count-response.model';

@Injectable({
  providedIn: 'root',
})
export class NotificationApiService {
  private readonly http = inject(HttpClient);

  private readonly notificationsUrl = `${environment.apiBaseUrl}/api/notifications`;

  getMyNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.notificationsUrl}/my`);
  }

  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.notificationsUrl}/my/unread`);
  }

  getUnreadCount(): Observable<UnreadCountResponse> {
    return this.http.get<UnreadCountResponse>(`${this.notificationsUrl}/my/unread-count`);
  }

  markAsRead(notificationId: number): Observable<Notification> {
    return this.http.put<Notification>(`${this.notificationsUrl}/${notificationId}/read`, {});
  }

  markAllAsRead(): Observable<MarkAllReadResponse> {
    return this.http.put<MarkAllReadResponse>(`${this.notificationsUrl}/my/read-all`, {});
  }
};
