import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CustomerProfile } from './customer-profile';
import { CustomerProfileApiService } from '../../services/customer-profile-api.service';
import { of } from 'rxjs';

describe('CustomerProfile Component', () => {
  let component: CustomerProfile;
  let fixture: ComponentFixture<CustomerProfile>;
  let mockApi: jasmine.SpyObj<CustomerProfileApiService>;

  const mockProfile = {
    customerId: 101,
    userId: 1,
    fullName: 'John Doe',
    email: 'john@example.com',
    phone: '1234567890',
    address: '123 Main Street',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  };

  beforeEach(async () => {
    mockApi = jasmine.createSpyObj('CustomerProfileApiService', ['getProfile', 'createProfile', 'updateProfile']);
    mockApi.getProfile.and.returnValue(of(mockProfile));

    await TestBed.configureTestingModule({
      imports: [CustomerProfile],
      providers: [
        { provide: CustomerProfileApiService, useValue: mockApi }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CustomerProfile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create customer profile component', () => {
    expect(component).toBeTruthy();
  });

  it('should load profile on init', () => {
    expect(mockApi.getProfile).toHaveBeenCalled();
    expect(component.profile()).toEqual(mockProfile);
    expect(component.loading()).toBeFalse();
  });

  it('should populate form when editing starts', () => {
    component.startEditing();
    expect(component.editing()).toBeTrue();
    expect(component.profileForm.value.fullName).toBe('John Doe');
  });
});
