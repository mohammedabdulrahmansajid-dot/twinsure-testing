import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClaimList } from './claim-list';
import { ClaimApiService } from '../../services/claim-api.service';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

describe('ClaimList Component', () => {
  let component: ClaimList;
  let fixture: ComponentFixture<ClaimList>;
  let mockApi: jasmine.SpyObj<ClaimApiService>;

  beforeEach(async () => {
    mockApi = jasmine.createSpyObj('ClaimApiService', ['getMyClaims']);
    mockApi.getMyClaims.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [ClaimList],
      providers: [
        { provide: ClaimApiService, useValue: mockApi },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClaimList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create claim list component', () => {
    expect(component).toBeTruthy();
  });

  it('should load claims on init', () => {
    expect(mockApi.getMyClaims).toHaveBeenCalled();
    expect(component.loading()).toBeFalse();
  });
});
