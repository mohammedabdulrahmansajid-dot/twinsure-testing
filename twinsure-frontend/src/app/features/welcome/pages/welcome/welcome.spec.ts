import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Welcome } from './welcome';
import { provideRouter } from '@angular/router';

describe('Welcome Component', () => {
  let component: Welcome;
  let fixture: ComponentFixture<Welcome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Welcome],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(Welcome);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create welcome component', () => {
    expect(component).toBeTruthy();
  });
});
