import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReviewedApplicationList } from './reviewed-application-list';
import { UnderwriterApplicationApiService } from '../../services/underwriter-application-api.service';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

describe('ReviewedApplicationList Component', () => {
  let component: ReviewedApplicationList;
  let fixture: ComponentFixture<ReviewedApplicationList>;
  let mockApi: jasmine.SpyObj<UnderwriterApplicationApiService>;

  beforeEach(async () => {
    mockApi = jasmine.createSpyObj('UnderwriterApplicationApiService', ['getReviewedApplications']);
    mockApi.getReviewedApplications.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [ReviewedApplicationList],
      providers: [
        { provide: UnderwriterApplicationApiService, useValue: mockApi },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReviewedApplicationList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create reviewed application list component', () => {
    expect(component).toBeTruthy();
  });

  it('should load reviewed applications on init', () => {
    expect(mockApi.getReviewedApplications).toHaveBeenCalled();
    expect(component.loading()).toBeFalse();
  });
});
