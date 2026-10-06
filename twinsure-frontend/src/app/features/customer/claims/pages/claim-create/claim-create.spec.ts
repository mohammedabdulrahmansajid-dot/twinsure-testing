import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClaimCreate } from './claim-create';
import { ClaimApiService } from '../../services/claim-api.service';
import { IncidentApiService } from '../../../incidents/services/incident-api.service';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

describe('ClaimCreate Component', () => {
  let component: ClaimCreate;
  let fixture: ComponentFixture<ClaimCreate>;
  let mockClaimApi: jasmine.SpyObj<ClaimApiService>;
  let mockIncidentApi: jasmine.SpyObj<IncidentApiService>;

  beforeEach(async () => {
    mockClaimApi = jasmine.createSpyObj('ClaimApiService', ['createClaim']);
    mockIncidentApi = jasmine.createSpyObj('IncidentApiService', ['getIncidentDetails']);

    mockIncidentApi.getIncidentDetails.and.returnValue(of({
      incident: {
        incidentId: 5,
        policyId: 1,
        twinId: 1,
        incidentType: 'SERVICE_OUTAGE',
        severity: 'HIGH',
        status: 'REPORTED',
        detectedAt: '2026-01-01',
        reportedAt: '2026-01-01',
        lossAmount: 500,
        description: 'Outage',
        impactAnalysis: 'None'
      },
      policyNumber: 'POL123',
      twinName: 'Twin 1',
      claim: null
    }));

    await TestBed.configureTestingModule({
      imports: [ClaimCreate],
      providers: [
        { provide: ClaimApiService, useValue: mockClaimApi },
        { provide: IncidentApiService, useValue: mockIncidentApi },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) => key === 'incidentId' ? '5' : null
              }
            }
          }
        },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClaimCreate);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create claim create component', () => {
    expect(component).toBeTruthy();
  });

  it('should load incident details on initialization', () => {
    expect(mockIncidentApi.getIncidentDetails).toHaveBeenCalledWith(5);
    expect(component.incident()?.incidentId).toBe(5);
  });
});
