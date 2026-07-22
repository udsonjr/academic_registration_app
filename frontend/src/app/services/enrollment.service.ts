import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreateEnrollmentRequest,
  Enrollment,
  PageParams,
  PageResponse,
} from '../models';
import { toHttpParams } from './user.service';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private readonly baseUrl = `${environment.apiBaseUrl}/enrollments`;

  constructor(private readonly http: HttpClient) {}

  list(
    params: PageParams & { userPublicId?: string; classGroupPublicId?: string } = {},
  ): Observable<PageResponse<Enrollment>> {
    return this.http.get<PageResponse<Enrollment>>(this.baseUrl, {
      params: toHttpParams(params),
    });
  }

  create(payload: CreateEnrollmentRequest): Observable<Enrollment> {
    return this.http.post<Enrollment>(this.baseUrl, payload);
  }

  confirm(publicId: string): Observable<Enrollment> {
    return this.http.patch<Enrollment>(`${this.baseUrl}/${publicId}/confirm`, {});
  }

  cancel(publicId: string): Observable<Enrollment> {
    return this.http.patch<Enrollment>(`${this.baseUrl}/${publicId}/cancel`, {});
  }
}
