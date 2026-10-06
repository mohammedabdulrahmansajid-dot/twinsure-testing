import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AiTwinList } from './ai-twin-list';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { provideRouter } from '@angular/router';
import { selectAiTwins, selectError, selectLoading } from '../../state/ai-twin.selectors';
import { AiTwinActions } from '../../state/ai-twin.actions';

describe('AiTwinList Component', () => {
  let component: AiTwinList;
  let fixture: ComponentFixture<AiTwinList>;
  let store: MockStore;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AiTwinList],
      providers: [
        provideMockStore({
          selectors: [
            { selector: selectAiTwins, value: [] },
            { selector: selectLoading, value: false },
            { selector: selectError, value: null }
          ]
        }),
        provideRouter([])
      ]
    }).compileComponents();

    store = TestBed.inject(MockStore);
    spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(AiTwinList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create AI twin list component', () => {
    expect(component).toBeTruthy();
  });

  it('should dispatch loadMyAiTwins action on init', () => {
    expect(store.dispatch).toHaveBeenCalledWith(AiTwinActions.loadMyAiTwins());
  });
});
