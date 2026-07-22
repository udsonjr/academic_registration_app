import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ClassGroupService } from './class-group.service';
import { environment } from '../../environments/environment';

describe('ClassGroupService', () => {
  let service: ClassGroupService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ClassGroupService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists class groups with filters and sort', () => {
    service
      .list({
        page: 0,
        size: 10,
        name: 'Turma',
        openForEnrollment: true,
        coursePublicId: 'c-1',
        subjectPublicId: 's-1',
        sort: 'createdAt,desc',
      })
      .subscribe();

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/class-groups` &&
        r.params.get('name') === 'Turma' &&
        r.params.get('openForEnrollment') === 'true' &&
        r.params.get('coursePublicId') === 'c-1' &&
        r.params.get('subjectPublicId') === 's-1' &&
        r.params.get('sort') === 'createdAt,desc',
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });
});
