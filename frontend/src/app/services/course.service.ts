import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Course,
  CreateCourseRequest,
  PageParams,
  PageResponse,
  UpdateCourseRequest,
} from '../models';
import { toHttpParams } from './user.service';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private readonly baseUrl = `${environment.apiBaseUrl}/courses`;

  constructor(private readonly http: HttpClient) {}

  list(
    params: PageParams & { name?: string; active?: boolean | '' } = {},
  ): Observable<PageResponse<Course>> {
    return this.http.get<PageResponse<Course>>(this.baseUrl, {
      params: toHttpParams(params),
    });
  }

  getById(publicId: string): Observable<Course> {
    return this.http.get<Course>(`${this.baseUrl}/${publicId}`);
  }

  create(payload: CreateCourseRequest): Observable<Course> {
    return this.http.post<Course>(this.baseUrl, payload);
  }

  update(publicId: string, payload: UpdateCourseRequest): Observable<Course> {
    return this.http.patch<Course>(`${this.baseUrl}/${publicId}`, payload);
  }

  delete(publicId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${publicId}`);
  }
}
