import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreateUserRequest,
  PageParams,
  PageResponse,
  UpdateUserRequest,
  User,
  UserRole,
} from '../models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly baseUrl = `${environment.apiBaseUrl}/users`;

  constructor(private readonly http: HttpClient) {}

  list(
    params: PageParams & { q?: string; role?: UserRole | '' } = {},
  ): Observable<PageResponse<User>> {
    return this.http.get<PageResponse<User>>(this.baseUrl, {
      params: toHttpParams(params),
    });
  }

  getById(publicId: string): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${publicId}`);
  }

  create(payload: CreateUserRequest): Observable<User> {
    return this.http.post<User>(this.baseUrl, payload);
  }

  update(publicId: string, payload: UpdateUserRequest): Observable<User> {
    return this.http.patch<User>(`${this.baseUrl}/${publicId}`, payload);
  }

  delete(publicId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${publicId}`);
  }
}

export function toHttpParams(params: object): HttpParams {
  let httpParams = new HttpParams();
  Object.entries(params as Record<string, unknown>).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      httpParams = httpParams.set(key, String(value));
    }
  });
  return httpParams;
}
