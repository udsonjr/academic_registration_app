import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthResponse, User } from '../models';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  const user: User = {
    publicId: 'u-1',
    name: 'Admin',
    email: 'admin@admin',
    role: 'ADMIN',
  };

  const authResponse: AuthResponse = {
    accessToken: 'token-123',
    tokenType: 'Bearer',
    expiresIn: 3600,
    user,
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('persists session on login', () => {
    service.login({ email: 'admin@admin', password: 'admin' }).subscribe((res) => {
      expect(res.accessToken).toBe('token-123');
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(authResponse);

    expect(service.getToken()).toBe('token-123');
    expect(service.currentUser()?.email).toBe('admin@admin');
    expect(service.isAuthenticated()).toBeTrue();
    expect(service.isAdmin()).toBeTrue();
  });

  it('clears session on logout and navigates to login', () => {
    localStorage.setItem('academic_access_token', 'token-123');
    localStorage.setItem('academic_user', JSON.stringify(user));
    service.setCurrentUser(user);
    const navigateSpy = spyOn(router, 'navigate').and.resolveTo(true);

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.currentUser()).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('registers without storing session', () => {
    service
      .register({
        name: 'Aluno',
        email: 'aluno@example.com',
        password: '123456',
        confirmPassword: '123456',
      })
      .subscribe((created) => {
        expect(created.email).toBe('aluno@example.com');
      });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    req.flush({ ...user, role: 'STUDENT', email: 'aluno@example.com', name: 'Aluno' });

    expect(service.getToken()).toBeNull();
  });
});
