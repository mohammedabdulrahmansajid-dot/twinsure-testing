import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RoleDashboard } from './role-dashboard';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { ActivatedRoute } from '@angular/router';
import { AuthActions } from '../../../features/auth/state/auth.actions';
import { selectUser } from '../../../features/auth/state/auth.selectors';

describe('RoleDashboard Component', () => {
  let component: RoleDashboard;
  let fixture: ComponentFixture<RoleDashboard>;
  let store: MockStore;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoleDashboard],
      providers: [
        provideMockStore({
          selectors: [
            { selector: selectUser, value: { username: 'testuser', role: 'CUSTOMER' } }
          ]
        }),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                title: 'Customer Dashboard',
                description: 'Manage policies and claims'
              }
            }
          }
        }
      ]
    }).compileComponents();

    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(RoleDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create role dashboard component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize title and description from route data', () => {
    expect(component.title()).toBe('Customer Dashboard');
    expect(component.description()).toBe('Manage policies and claims');
  });

  it('should dispatch logout action when logout is triggered', () => {
    component.logout();
    expect(store.dispatch).toHaveBeenCalledWith(AuthActions.logout());
  });
});
