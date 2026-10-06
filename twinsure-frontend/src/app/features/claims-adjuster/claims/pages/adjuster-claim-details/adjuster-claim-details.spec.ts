import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdjusterClaimDetails } from './adjuster-claim-details';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import {
  selectContextError,
  selectContextLoading,
  selectError,
  selectLoading,
  selectReviewContext,
  selectSaving,
  selectSelectedDetails,
} from '../../state/claims-adjuster.selectors';

describe('AdjusterClaimDetails Component', () => {
  let component: AdjusterClaimDetails;
  let fixture: ComponentFixture<AdjusterClaimDetails>;
  let store: MockStore;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdjusterClaimDetails],
      providers: [
        provideMockStore({
          selectors: [
            { selector: selectSelectedDetails, value: null },
            { selector: selectReviewContext, value: null },
            { selector: selectLoading, value: false },
            { selector: selectContextLoading, value: false },
            { selector: selectSaving, value: false },
            { selector: selectError, value: null },
            { selector: selectContextError, value: null }
          ]
        }),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => key === 'claimId' ? '12' : null
              }
            }
          }
        },
        provideRouter([])
      ]
    }).compileComponents();

    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(AdjusterClaimDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create adjuster claim details component', () => {
    expect(component).toBeTruthy();
  });

  it('should validate status checks', () => {
    expect(component.canStartReview('ASSIGNED')).toBeTrue();
    expect(component.canMakeDecision('UNDER_REVIEW')).toBeTrue();
    expect(component.canClose('APPROVED')).toBeTrue();
  });
});
