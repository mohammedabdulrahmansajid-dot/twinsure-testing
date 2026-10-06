import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Register } from './register';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { AuthActions } from '../../state/auth.actions';
import { selectError, selectLoading } from '../../state/auth.selectors';

describe('Register Component', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let store: MockStore;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        provideMockStore({
          selectors: [
            { selector: selectLoading, value: false },
            { selector: selectError, value: null }
          ]
        })
      ]
    }).compileComponents();

    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create register component', () => {
    expect(component).toBeTruthy();
  });

  it('should invalidate matching passwords if confirmPassword differs', () => {
    component.registerForm.controls.username.setValue('john_doe');
    component.registerForm.controls.password.setValue('password123');
    component.registerForm.controls.confirmPassword.setValue('different123');

    expect(component.registerForm.invalid).toBeTrue();
    expect(component.registerForm.hasError('passwordMismatch')).toBeTrue();
  });

  it('should dispatch register action when form is valid', () => {
    component.registerForm.controls.username.setValue('john_doe');
    component.registerForm.controls.password.setValue('password123');
    component.registerForm.controls.confirmPassword.setValue('password123');

    component.submit();

    expect(store.dispatch).toHaveBeenCalledWith(
      AuthActions.register({
        request: { username: 'john_doe', password: 'password123' }
      })
    );
  });
});
