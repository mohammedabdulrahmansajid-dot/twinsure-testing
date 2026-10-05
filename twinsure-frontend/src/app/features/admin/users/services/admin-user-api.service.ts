// Provides Admin user-management requests through the API Gateway.
// Identity Service remains responsible for role restrictions,
// username uniqueness, password encoding, and account status changes.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import {
  AdminUser,
  CreateStaffUserRequest,
  UpdateUserStatusRequest,
} from '../models/admin-user.model';

@Injectable({
  providedIn: 'root',
})
export class AdminUserApiService {
  private readonly http = inject(HttpClient);

  private readonly usersUrl = `${environment.apiBaseUrl}/api/admin/users`;

  getAllUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(this.usersUrl);
  }

  getUserById(userId: number): Observable<AdminUser> {
    return this.http.get<AdminUser>(`${this.usersUrl}/${userId}`);
  }

  createStaffUser(request: CreateStaffUserRequest): Observable<AdminUser> {
    return this.http.post<AdminUser>(this.usersUrl, request);
  }

  updateUserStatus(userId: number, request: UpdateUserStatusRequest): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.usersUrl}/${userId}/status`, request);
  }
}
