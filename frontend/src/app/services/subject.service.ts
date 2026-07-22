import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreateSubjectRequest,
  PageParams,
  PageResponse,
  Subject,
  UpdateSubjectRequest,
} from '../models';
import { toHttpParams } from './user.service';

@Injectable({ providedIn: 'root' })
export class SubjectService {
  private readonly baseUrl = `${environment.apiBaseUrl}/subjects`;

  constructor(private readonly http: HttpClient) {}

  list(
    params: PageParams & { name?: string; coursePublicId?: string } = {},
  ): Observable<PageResponse<Subject>> {
    return this.http.get<PageResponse<Subject>>(this.baseUrl, {
      params: toHttpParams(params),
    });
  }

  getById(publicId: string): Observable<Subject> {
    return this.http.get<Subject>(`${this.baseUrl}/${publicId}`);
  }

  create(payload: CreateSubjectRequest): Observable<Subject> {
    return this.http.post<Subject>(this.baseUrl, payload);
  }

  update(publicId: string, payload: UpdateSubjectRequest): Observable<Subject> {
    return this.http.patch<Subject>(`${this.baseUrl}/${publicId}`, payload);
  }

  delete(publicId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${publicId}`);
  }
}
