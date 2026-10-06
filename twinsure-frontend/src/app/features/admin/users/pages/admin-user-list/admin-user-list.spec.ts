// // import { test, expect } from '@playwright/test';

// // test('Admin User List loads successfully', async ({ page }) => {

// //   await page.goto('http://localhost:4200/admin/users');

// //   await expect(page).toHaveURL(/admin\/users/);

// //   // Verify page heading
// //   await expect(
// //     page.getByRole('heading', { name: /admin/i })
// //   ).toBeVisible();

// //   // Verify user table is present
// //   await expect(
// //     page.locator('table')
// //   ).toBeVisible();

// });

// import { ComponentFixture, TestBed } from '@angular/core/testing';
// import { AdminUserList } from './admin-user-list';
// import { AdminUserApiService } from '../../services/admin-user-api.service';
// import { provideMockStore, MockStore } from '@ngrx/store/testing';
// import { provideRouter } from '@angular/router';
// import { of } from 'rxjs';

// describe('AdminUserList Component', () => {
//   let component: AdminUserList;
//   let fixture: ComponentFixture<AdminUserList>;
//   let mockApi: jasmine.SpyObj<AdminUserApiService>;

//   beforeEach(async () => {
//     mockApi = jasmine.createSpyObj('AdminUserApiService', ['getAllUsers', 'updateUserStatus']);
//     mockApi.getAllUsers.and.returnValue(of([]));

//     await TestBed.configureTestingModule({
//       imports: [AdminUserList],
//       providers: [
//         { provide: AdminUserApiService, useValue: mockApi },
//         provideMockStore(),
//         provideRouter([])
//       ]
//     }).compileComponents();

//     fixture = TestBed.createComponent(AdminUserList);
//     component = fixture.componentInstance;
//     fixture.detectChanges();
//   });

//   it('should create admin user list component', () => {
//     expect(component).toBeTruthy();
//   });

//   it('should load users on init', () => {
//     expect(mockApi.getAllUsers).toHaveBeenCalled();
//     expect(component.loading()).toBeFalse();
//   });
// });
