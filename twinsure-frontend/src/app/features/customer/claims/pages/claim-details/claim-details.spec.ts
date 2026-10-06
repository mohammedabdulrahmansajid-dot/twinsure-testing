import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClaimDetails } from './claim-details';
import { ClaimApiService } from '../../services/claim-api.service';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

describe('ClaimDetails Component', () => {
  let component: ClaimDetails;
  let fixture: ComponentFixture<ClaimDetails>;
  let mockApi: jasmine.SpyObj<ClaimApiService>;

  beforeEach(async () => {
    mockApi = jasmine.createSpyObj('ClaimApiService', ['getClaimDetails', 'addDocument']);
    mockApi.getClaimDetails.and.returnValue(of({
      claim: {
        claimId: 10,
        incidentId: 5,
        policyId: 1,
        twinId: 1,
        claimNumber: 'CLM100',
        claimedAmount: 1000,
        approvedAmount: null,
        status: 'SUBMITTED',
        assignedAdjusterId: null,
        submittedAt: '2026-01-01',
        updatedAt: '2026-01-01'
      },
      incidentType: 'OUTAGE',
      severity: 'HIGH',
      policyNumber: 'POL1',
      twinName: 'Twin 1',
      adjusterUsername: null,
      documents: [],
      decisions: []
    }));

    await TestBed.configureTestingModule({
      imports: [ClaimDetails],
      providers: [
        { provide: ClaimApiService, useValue: mockApi },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => key === 'claimId' ? '10' : null
              }
            }
          }
        },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClaimDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create claim details component', () => {
    expect(component).toBeTruthy();
  });

  it('should load claim details on initialization', () => {
    expect(mockApi.getClaimDetails).toHaveBeenCalledWith(10);
    expect(component.details()?.claim.claimId).toBe(10);
  });
});
