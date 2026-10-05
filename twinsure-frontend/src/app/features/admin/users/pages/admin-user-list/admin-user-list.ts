// Displays all TwinSure accounts and supports Admin status management.
// Search and role filters run locally while status updates are always
// validated and persisted through Identity Service.

import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Store } from '@ngrx/store';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { selectUser } from '../../../../auth/state/auth.selectors';
import { AdminManagedRole, AdminUser, AdminUserStatus } from '../../models/admin-user.model';
import { AdminUserApiService } from '../../services/admin-user-api.service';

type RoleFilter = 'ALL' | AdminManagedRole;

type StatusFilter = 'ALL' | AdminUserStatus;

@Component({
  selector: 'app-admin-user-list',

  imports: [FormsModule, RouterLink],

  templateUrl: './admin-user-list.html',

  styleUrl: './admin-user-list.css',

  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminUserList implements OnInit {
  private readonly userApi = inject(AdminUserApiService);

  private readonly store = inject(Store);

  readonly authenticatedUser = this.store.selectSignal(selectUser);

  readonly users = signal<AdminUser[]>([]);

  readonly loading = signal(true);

  readonly error = signal<string | null>(null);

  readonly success = signal<string | null>(null);

  readonly updatingUserId = signal<number | null>(null);

  readonly searchText = signal('');

  readonly roleFilter = signal<RoleFilter>('ALL');

  readonly statusFilter = signal<StatusFilter>('ALL');

  readonly roleOptions: RoleFilter[] = [
    'ALL',
    'CUSTOMER',
    'UNDERWRITER',
    'CLAIMS_ADJUSTER',
    'ADMIN',
  ];

  readonly statusOptions: StatusFilter[] = ['ALL', 'ACTIVE', 'SUSPENDED'];

  readonly filteredUsers = computed(() => {
    const search = this.searchText().trim().toLowerCase();

    const role = this.roleFilter();

    const status = this.statusFilter();

    return this.users()
      .filter((user) => {
        const searchMatches =
          search.length === 0 ||
          user.username.toLowerCase().includes(search) ||
          user.userId.toString().includes(search) ||
          user.role.toLowerCase().includes(search);

        const roleMatches = role === 'ALL' || user.role === role;

        const statusMatches = status === 'ALL' || user.status === status;

        return searchMatches && roleMatches && statusMatches;
      })
      .sort((first, second) => first.userId - second.userId);
  });

  readonly activeCount = computed(
    () => this.users().filter((user) => user.status === 'ACTIVE').length,
  );

  readonly suspendedCount = computed(
    () => this.users().filter((user) => user.status === 'SUSPENDED').length,
  );

  readonly staffCount = computed(
    () =>
      this.users().filter((user) => user.role === 'UNDERWRITER' || user.role === 'CLAIMS_ADJUSTER')
        .length,
  );

  ngOnInit(): void {
    this.loadUsers();
  }

  reload(): void {
    this.loadUsers();
  }

  updateSearch(value: string): void {
    this.searchText.set(value);
  }

  updateRoleFilter(value: string): void {
    this.roleFilter.set(value as RoleFilter);
  }

  updateStatusFilter(value: string): void {
    this.statusFilter.set(value as StatusFilter);
  }

  clearFilters(): void {
    this.searchText.set('');
    this.roleFilter.set('ALL');
    this.statusFilter.set('ALL');
  }

  toggleUserStatus(user: AdminUser): void {
    this.error.set(null);

    this.success.set(null);

    if (this.isCurrentUser(user)) {
      this.error.set(
        'You cannot change the status of your own active Admin account from this page.',
      );

      return;
    }

    const newStatus: AdminUserStatus = user.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';

    this.updatingUserId.set(user.userId);

    this.userApi
      .updateUserStatus(user.userId, {
        status: newStatus,
      })
      .pipe(finalize(() => this.updatingUserId.set(null)))
      .subscribe({
        next: (updatedUser) => {
          this.users.update((users) =>
            users.map((existingUser) =>
              existingUser.userId === updatedUser.userId ? updatedUser : existingUser,
            ),
          );

          this.success.set(
            `${updatedUser.username} is now ${this.formatLabel(updatedUser.status)}.`,
          );
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }

  isCurrentUser(user: AdminUser): boolean {
    return this.authenticatedUser()?.userId === user.userId;
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private loadUsers(): void {
    this.loading.set(true);

    this.error.set(null);

    this.success.set(null);

    this.userApi
      .getAllUsers()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (users) => {
          this.users.set(users);
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }
}
