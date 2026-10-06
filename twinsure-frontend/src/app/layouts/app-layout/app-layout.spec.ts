import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppLayout } from './app-layout';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { provideRouter } from '@angular/router';
import { selectUser } from '../../features/auth/state/auth.selectors';
import { selectUnreadCount } from '../../features/notifications/state/notification.selectors';
import { NotificationActions } from '../../features/notifications/state/notification.actions';
import { AuthActions } from '../../features/auth/state/auth.actions';

describe('AppLayout Component', () => {
  let component: AppLayout;
  let fixture: ComponentFixture<AppLayout>;
  let store: MockStore;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppLayout],
      providers: [
        provideMockStore({
          selectors: [
            { selector: selectUser, value: { username: 'test_customer', role: 'CUSTOMER' } },
            { selector: selectUnreadCount, value: 3 }
          ]
        }),
        provideRouter([])
      ]
    }).compileComponents();

    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(AppLayout);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create app layout component', () => {
    expect(component).toBeTruthy();
  });

  it('should load unread notifications count on init', () => {
    expect(store.dispatch).toHaveBeenCalledWith(NotificationActions.loadUnreadCount());
  });

  it('should compute role label for customer', () => {
    expect(component.roleLabel()).toBe('Customer');
  });

  it('should compute customer navigation items', () => {
    const nav = component.navigationItems();
    expect(nav.length).toBeGreaterThan(0);
    expect(nav[0].label).toBe('Dashboard');
  });

  it('should toggle sidebar state', () => {
    component.openSidebar();
    expect(component.sidebarOpen()).toBeTrue();
    component.closeSidebar();
    expect(component.sidebarOpen()).toBeFalse();
  });

  it('should dispatch logout action', () => {
    component.logout();
    expect(store.dispatch).toHaveBeenCalledWith(AuthActions.logout());
  });
});
