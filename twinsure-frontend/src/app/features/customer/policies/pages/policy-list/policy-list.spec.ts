import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PolicyList } from './policy-list';
import { PolicyApiService } from '../../services/policy-api.service';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

describe('PolicyList Component', () => {
  let component: PolicyList;
  let fixture: ComponentFixture<PolicyList>;
  let mockApi: jasmine.SpyObj<PolicyApiService>;

  beforeEach(async () => {
    mockApi = jasmine.createSpyObj('PolicyApiService', ['getMyPolicies']);
    mockApi.getMyPolicies.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [PolicyList],
      providers: [
        { provide: PolicyApiService, useValue: mockApi },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PolicyList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create policy list component', () => {
    expect(component).toBeTruthy();
  });

  it('should load policies on init', () => {
    expect(mockApi.getMyPolicies).toHaveBeenCalled();
    expect(component.loading()).toBeFalse();
  });
});
