import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserService, toHttpParams } from '../services/user.service';
import { environment } from '../../environments/environment';

describe('toHttpParams', () => {
  it('omits empty, null and undefined values', () => {
    const params = toHttpParams({
      page: 0,
      size: 10,
      sort: 'name,asc',
      q: '',
      role: undefined,
      active: null,
    });

    expect(params.get('page')).toBe('0');
    expect(params.get('size')).toBe('10');
    expect(params.get('sort')).toBe('name,asc');
    expect(params.has('q')).toBeFalse();
    expect(params.has('role')).toBeFalse();
    expect(params.has('active')).toBeFalse();
  });
});

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists users with filters and sort', () => {
    service.list({ page: 0, size: 10, q: 'ana', role: 'STUDENT', sort: 'name,asc' }).subscribe();

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/users` &&
        r.params.get('q') === 'ana' &&
        r.params.get('role') === 'STUDENT' &&
        r.params.get('sort') === 'name,asc',
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });
});
