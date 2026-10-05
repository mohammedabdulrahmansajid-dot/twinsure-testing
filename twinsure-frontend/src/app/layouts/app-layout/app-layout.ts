// Provides the permanent authenticated TwinSure application shell.
// The layout loads the unread notification count, displays role-specific
// navigation, and renders the active feature inside its child route outlet.

import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { Store } from '@ngrx/store';

import { NavigationItem } from '../../core/models/navigation-item.model';
import { AuthActions } from '../../features/auth/state/auth.actions';
import { selectUser } from '../../features/auth/state/auth.selectors';
import { NotificationActions } from '../../features/notifications/state/notification.actions';
import { selectUnreadCount } from '../../features/notifications/state/notification.selectors';

@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app-layout.html',
  styleUrl: './app-layout.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppLayout implements OnInit {
  private readonly store = inject(Store);

  readonly user = this.store.selectSignal(selectUser);

  readonly unreadCount = this.store.selectSignal(selectUnreadCount);

  readonly sidebarOpen = signal(false);

  readonly roleLabel = computed(() => {
    const role = this.user()?.role;

    switch (role) {
      case 'CUSTOMER':
        return 'Customer';

      case 'UNDERWRITER':
        return 'Underwriter';

      case 'CLAIMS_ADJUSTER':
        return 'Claims Adjuster';

      case 'ADMIN':
        return 'Administrator';

      default:
        return 'TwinSure';
    }
  });

  readonly navigationItems = computed<NavigationItem[]>(() => {
    switch (this.user()?.role) {
      case 'CUSTOMER':
        return this.customerNavigation;

      case 'UNDERWRITER':
        return this.underwriterNavigation;

      case 'CLAIMS_ADJUSTER':
        return this.adjusterNavigation;

      case 'ADMIN':
        return this.adminNavigation;

      default:
        return [];
    }
  });

  private readonly customerNavigation: NavigationItem[] = [
    {
      label: 'Dashboard',
      shortLabel: 'DB',
      route: '/customer/dashboard',
      exact: true,
    },
    {
      label: 'Profile',
      shortLabel: 'PR',
      route: '/customer/profile',
      exact: false,
    },
    {
      label: 'AI Twins',
      shortLabel: 'AI',
      route: '/customer/ai-twins',
      exact: false,
    },
    {
      label: 'Insurance Products',
      shortLabel: 'IP',
      route: '/customer/insurance-products',
      exact: false,
    },
    {
      label: 'Applications',
      shortLabel: 'PA',
      route: '/customer/policy-applications',
      exact: false,
    },
    {
      label: 'Policies',
      shortLabel: 'PO',
      route: '/customer/policies',
      exact: false,
    },
    
    {
      label: 'Simulate Action',
      shortLabel: 'SA',
      route: '/customer/ai-actions/simulate',
      exact: true,
    },
    {
      label: 'Action History',
      shortLabel: 'AH',
      route: '/customer/ai-actions',
      exact: true,
    },
    {
      label: 'Incidents',
      shortLabel: 'IN',
      route: '/customer/incidents',
      exact: false,
    },
    {
      label: 'Claims',
      shortLabel: 'CL',
      route: '/customer/claims',
      exact: false,
    },
  ];

  private readonly underwriterNavigation: NavigationItem[] = [
    {
      label: 'Dashboard',
      shortLabel: 'DB',
      route: '/underwriter/dashboard',
      exact: true,
    },
    {
      label: 'Pending Applications',
      shortLabel: 'PE',
      route: '/underwriter/applications/pending',
      exact: false,
    },
    {
      label: 'Reviewed Applications',
      shortLabel: 'RE',
      route: '/underwriter/applications/reviewed',
      exact: false,
    },
  ];

  private readonly adjusterNavigation: NavigationItem[] = [
    {
      label: 'Dashboard',
      shortLabel: 'DB',
      route: '/claims-adjuster/dashboard',
      exact: true,
    },
    {
      label: 'Assigned Claims',
      shortLabel: 'CL',
      route: '/claims-adjuster/claims',
      exact: false,
    },
  ];

  private readonly adminNavigation: NavigationItem[] = [
    {
      label: 'Dashboard',
      shortLabel: 'DB',
      route: '/admin/dashboard',
      exact: true,
    },
    {
      label: 'Users',
      shortLabel: 'US',
      route: '/admin/users',
      exact: false,
    },
    {
      label: 'Customers',
      shortLabel: 'CU',
      route: '/admin/customers',
      exact: false,
    },
    {
      label: 'AI Twins',
      shortLabel: 'AI',
      route: '/admin/ai-twins',
      exact: false,
    },
    {
      label: 'Insurance Products',
      shortLabel: 'IP',
      route: '/admin/insurance-products',
      exact: false,
    },
    {
      label: 'Policy Applications',
      shortLabel: 'PA',
      route: '/admin/policy-applications',
      exact: false,
    },
    {
      label: 'Policies',
      shortLabel: 'PO',
      route: '/admin/policies',
      exact: false,
    },
    {
      label: 'AI Actions',
      shortLabel: 'AC',
      route: '/admin/ai-actions',
      exact: false,
    },
    {
      label: 'Incidents',
      shortLabel: 'IN',
      route: '/admin/incidents',
      exact: false,
    },
    {
      label: 'Claims',
      shortLabel: 'CL',
      route: '/admin/claims',
      exact: false,
    },
  ];

  ngOnInit(): void {
    this.store.dispatch(NotificationActions.loadUnreadCount());
  }

  openSidebar(): void {
    this.sidebarOpen.set(true);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
