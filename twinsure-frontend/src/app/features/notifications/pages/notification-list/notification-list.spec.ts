import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NotificationList } from './notification-list';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import {
  selectError,
  selectLoading,
  selectNotifications,
  selectUnreadCount,
  selectUpdating,
} from '../../state/notification.selectors';
import { NotificationActions } from '../../state/notification.actions';

describe('NotificationList Component', () => {
  let component: NotificationList;
  let fixture: ComponentFixture<NotificationList>;
  let store: MockStore;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotificationList],
      providers: [
        provideMockStore({
          selectors: [
            { selector: selectNotifications, value: [] },
            { selector: selectUnreadCount, value: 0 },
            { selector: selectLoading, value: false },
            { selector: selectUpdating, value: false },
            { selector: selectError, value: null }
          ]
        })
      ]
    }).compileComponents();

    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(NotificationList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create notification list component', () => {
    expect(component).toBeTruthy();
  });

  it('should dispatch load actions on init', () => {
    expect(store.dispatch).toHaveBeenCalledWith(NotificationActions.loadNotifications());
    expect(store.dispatch).toHaveBeenCalledWith(NotificationActions.loadUnreadCount());
  });

  it('should dispatch markAsRead when clicked', () => {
    component.markAsRead(42);
    expect(store.dispatch).toHaveBeenCalledWith(NotificationActions.markAsRead({ notificationId: 42 }));
  });
});
