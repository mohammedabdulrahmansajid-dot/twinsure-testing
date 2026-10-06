import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PendingApplicationList } from './pending-application-list';
import { UnderwriterApplicationApiService } from '../../services/underwriter-application-api.service';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

describe('PendingApplicationList Component', () => {
  let component: PendingApplicationList;
  let fixture: ComponentFixture<PendingApplicationList>;
  let mockApi: jasmine.SpyObj<UnderwriterApplicationApiService>;

  beforeEach(async () => {
    mockApi = jasmine.createSpyObj('UnderwriterApplicationApiService', ['getPendingApplications']);
    mockApi.getPendingApplications.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [PendingApplicationList],
      providers: [
        { provide: UnderwriterApplicationApiService, useValue: mockApi },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PendingApplicationList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create pending application list component', () => {
    expect(component).toBeTruthy();
  });

  it('should load pending applications on init', () => {
    expect(mockApi.getPendingApplications).toHaveBeenCalled();
    expect(component.loading()).toBeFalse();
  });
});
