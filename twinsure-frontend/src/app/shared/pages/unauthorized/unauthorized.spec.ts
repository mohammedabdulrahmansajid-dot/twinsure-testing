import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Unauthorized } from './unauthorized';
import { provideRouter } from '@angular/router';

describe('Unauthorized Component', () => {
  let component: Unauthorized;
  let fixture: ComponentFixture<Unauthorized>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Unauthorized],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(Unauthorized);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create unauthorized component', () => {
    expect(component).toBeTruthy();
  });
});
