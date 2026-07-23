import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('auth guards', () => {
  let auth: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(() => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', [
      'isAuthenticated',
      'isAdmin',
    ]);
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
    router = TestBed.inject(Router);
  });

  it('authGuard allows authenticated users', () => {
    auth.isAuthenticated.and.returnValue(true);
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result).toBeTrue();
  });

  it('authGuard redirects guests to login', () => {
    auth.isAuthenticated.and.returnValue(false);
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result).toEqual(router.createUrlTree(['/login']));
  });

  it('guestGuard allows unauthenticated users', () => {
    auth.isAuthenticated.and.returnValue(false);
    const result = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));
    expect(result).toBeTrue();
  });

  it('adminGuard allows admins', () => {
    auth.isAuthenticated.and.returnValue(true);
    auth.isAdmin.and.returnValue(true);
    const result = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));
    expect(result).toBeTrue();
  });

  it('adminGuard redirects students to enrollments', () => {
    auth.isAuthenticated.and.returnValue(true);
    auth.isAdmin.and.returnValue(false);
    const result = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));
    expect(result).toEqual(router.createUrlTree(['/enrollments']));
  });
});
