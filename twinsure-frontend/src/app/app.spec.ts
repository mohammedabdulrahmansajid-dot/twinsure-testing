import { ComponentFixture, TestBed } from '@angular/core/testing';
import { App } from './app';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { provideRouter } from '@angular/router';
import { AuthActions } from './features/auth/state/auth.actions';

describe('App Component', () => {
  let component: App;
  let fixture: ComponentFixture<App>;
  let store: MockStore;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideMockStore(),
        provideRouter([])
      ]
    }).compileComponents();

    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(App);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create root app component', () => {
    expect(component).toBeTruthy();
  });

  it('should dispatch restoreSession on init', () => {
    expect(store.dispatch).toHaveBeenCalledWith(AuthActions.restoreSession());
  });
});
