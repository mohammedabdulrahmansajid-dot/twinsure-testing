import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AssignedClaimList } from './assigned-claim-list';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { provideRouter } from '@angular/router';
import {
  selectActiveClaims,
  selectAssignedClaims,
  selectAssignedCount,
  selectCompletedClaims,
  selectCompletedCount,
  selectError,
  selectInformationRequiredCount,
  selectLoading,
  selectUnderReviewCount,
} from '../../state/claims-adjuster.selectors';
import { ClaimsAdjusterActions } from '../../state/claims-adjuster.actions';

describe('AssignedClaimList Component', () => {
  let component: AssignedClaimList;
  let fixture: ComponentFixture<AssignedClaimList>;
  let store: MockStore;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AssignedClaimList],
      providers: [
        provideMockStore({
          selectors: [
            { selector: selectAssignedClaims, value: [] },
            { selector: selectActiveClaims, value: [] },
            { selector: selectCompletedClaims, value: [] },
            { selector: selectAssignedCount, value: 0 },
            { selector: selectUnderReviewCount, value: 0 },
            { selector: selectInformationRequiredCount, value: 0 },
            { selector: selectCompletedCount, value: 0 },
            { selector: selectLoading, value: false },
            { selector: selectError, value: null }
          ]
        }),
        provideRouter([])
      ]
    }).compileComponents();

    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(AssignedClaimList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create assigned claim list component', () => {
    expect(component).toBeTruthy();
  });

  it('should dispatch loadAssignedClaims action on init', () => {
    expect(store.dispatch).toHaveBeenCalledWith(ClaimsAdjusterActions.loadAssignedClaims());
  });

  it('should return correct status labels', () => {
    expect(component.statusLabel('ASSIGNED')).toBe('Ready to Start');
    expect(component.statusLabel('UNDER_REVIEW')).toBe('Under Review');
  });
});
