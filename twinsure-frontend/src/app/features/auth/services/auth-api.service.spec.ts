import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthApiService } from './auth-api.service';
import { APPLICATION_ROLES } from '../../../core/constants/application-role';
import { environment } from '../../../../environments/environment';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthApiService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should post login payload and return response', () => {
    const mockRequest = { username: 'john_doe', password: 'Password123!' };
    const mockResponse = {
      message: 'Authentication successful',
      user: {
        userId: 1,
        username: 'john_doe',
        role: APPLICATION_ROLES.CUSTOMER,
        customerId: 10,
        status: 'ACTIVE',
      },
    };

    service.login(mockRequest).subscribe((res) => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockResponse);
  });

  it('should post registration payload and return response', () => {
    const mockRequest = { username: 'newuser', password: 'Password123!' };
    const mockResponse = {
      userId: 2,
      username: 'newuser',
      role: APPLICATION_ROLES.CUSTOMER,
      customerId: null,
      status: 'ACTIVE',
    };

    service.register(mockRequest).subscribe((res) => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockResponse);
  });

  it('should send logout request', () => {
    service.logout().subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/logout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush(null);
  });
});
