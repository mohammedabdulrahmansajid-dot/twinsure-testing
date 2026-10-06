import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UnderwriterApplicationDetails } from './underwriter-application-details';
import { UnderwriterApplicationApiService } from '../../services/underwriter-application-api.service';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

describe('UnderwriterApplicationDetails Component', () => {
  let component: UnderwriterApplicationDetails;
  let fixture: ComponentFixture<UnderwriterApplicationDetails>;
  let mockApi: jasmine.SpyObj<UnderwriterApplicationApiService>;

  beforeEach(async () => {
    mockApi = jasmine.createSpyObj('UnderwriterApplicationApiService', [
      'getApplicationDetails',
      'getRiskProfile'
    ]);

    mockApi.getApplicationDetails.and.returnValue(of({
      application: {
        applicationId: 1,
        productId: 10,
        twinId: 2,
        customerId: 3,
        status: 'PENDING_REVIEW',
        submittedAt: '2026-01-01',
        proposedPremium: 100,
        proposedCoverageLimit: 5000,
        proposedDeductible: 200,
        riskScore: null,
        riskLevel: null,
        systemRecommendation: null,
        decisionReason: null,
        reviewedAt: null,
        underwriterId: null
      },
      product: {
        productId: 10,
        name: 'Standard Twin Insurance',
        description: 'Protection plan',
        basePremium: 100,
        coverageLimit: 5000,
        deductible: 200,
        active: true
      },
      twin: {
        twinId: 2,
        customerId: 3,
        name: 'AI Agent Twin',
        provider: 'Custom',
        modelType: 'GPT-4',
        systemPrompt: 'System',
        monthlyBudget: 1000,
        maxPerTransactionLimit: 100,
        status: 'ACTIVE',
        createdAt: '2026-01-01',
        updatedAt: '2026-01-01'
      }
    }));

    mockApi.getRiskProfile.and.returnValue(of({
      twinId: 2,
      riskScore: 25,
      riskLevel: 'LOW',
      totalSimulatedActions: 10,
      failedActionsCount: 0,
      totalIncidentCount: 0,
      totalClaimCount: 0
    }));

    await TestBed.configureTestingModule({
      imports: [UnderwriterApplicationDetails],
      providers: [
        { provide: UnderwriterApplicationApiService, useValue: mockApi },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => key === 'applicationId' ? '1' : null
              }
            }
          }
        },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UnderwriterApplicationDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create underwriter application details component', () => {
    expect(component).toBeTruthy();
  });

  it('should load application details and risk profile on init', () => {
    expect(mockApi.getApplicationDetails).toHaveBeenCalledWith(1);
    expect(mockApi.getRiskProfile).toHaveBeenCalledWith(2);
    expect(component.details()?.application.applicationId).toBe(1);
  });
});
