import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Login } from './login';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { ActivatedRoute } from '@angular/router';
import { AuthActions } from '../../state/auth.actions';
import { selectError, selectLoading } from '../../state/auth.selectors';

describe('Login Component', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let store: MockStore;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideMockStore({
          selectors: [
            { selector: selectLoading, value: false },
            { selector: selectError, value: null }
          ]
        }),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) => key === 'registered' ? 'true' : null
              }
            }
          }
        }
      ]
    }).compileComponents();

    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create login component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values and invalid status', () => {
    expect(component.loginForm.valid).toBeFalse();
    expect(component.loginForm.controls.username.value).toBe('');
    expect(component.loginForm.controls.password.value).toBe('');
  });

  it('should validate username field', () => {
    const usernameCtrl = component.loginForm.controls.username;
    usernameCtrl.setValue('ab');
    expect(usernameCtrl.invalid).toBeTrue();
    usernameCtrl.setValue('validUser');
    expect(usernameCtrl.valid).toBeTrue();
  });

  it('should validate password length', () => {
    const passCtrl = component.loginForm.controls.password;
    passCtrl.setValue('12345');
    expect(passCtrl.invalid).toBeTrue();
    passCtrl.setValue('123456');
    expect(passCtrl.valid).toBeTrue();
  });

  it('should mark form controls as touched on invalid submit', () => {
    component.submit();
    expect(store.dispatch).toHaveBeenCalledWith(AuthActions.clearError());
    expect(component.loginForm.controls.username.touched).toBeTrue();
    expect(component.loginForm.controls.password.touched).toBeTrue();
  });

  it('should dispatch login action on valid submit', () => {
    component.loginForm.controls.username.setValue('john_doe');
    component.loginForm.controls.password.setValue('SecurePass123');

    component.submit();

    expect(store.dispatch).toHaveBeenCalledWith(
      AuthActions.login({
        request: { username: 'john_doe', password: 'SecurePass123' }
      })
    );
  });
});
