import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PolicyDetails } from './policy-details';
import { PolicyApiService } from '../../services/policy-api.service';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

describe('PolicyDetails Component', () => {
  let component: PolicyDetails;
  let fixture: ComponentFixture<PolicyDetails>;
  let mockApi: jasmine.SpyObj<PolicyApiService>;

  beforeEach(async () => {
    mockApi = jasmine.createSpyObj('PolicyApiService', ['getPolicyDetails']);
    mockApi.getPolicyDetails.and.returnValue(of(null));

    await TestBed.configureTestingModule({
      imports: [PolicyDetails],
      providers: [
        { provide: PolicyApiService, useValue: mockApi },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => key === 'policyId' ? '10' : null
              }
            }
          }
        },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PolicyDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create policy details component', () => {
    expect(component).toBeTruthy();
  });

  it('should format labels correctly', () => {
    expect(component.formatLabel('ACTIVE_POLICY')).toBe('Active Policy');
  });
});
