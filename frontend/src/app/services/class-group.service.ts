import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ClassGroup,
  CreateClassGroupRequest,
  PageParams,
  PageResponse,
  UpdateClassGroupRequest,
} from '../models';
import { toHttpParams } from './user.service';

@Injectable({ providedIn: 'root' })
export class ClassGroupService {
  private readonly baseUrl = `${environment.apiBaseUrl}/class-groups`;

  constructor(private readonly http: HttpClient) {}

  list(
    params: PageParams & { subjectPublicId?: string } = {},
  ): Observable<PageResponse<ClassGroup>> {
    return this.http.get<PageResponse<ClassGroup>>(this.baseUrl, {
      params: toHttpParams(params),
    });
  }

  getById(publicId: string): Observable<ClassGroup> {
    return this.http.get<ClassGroup>(`${this.baseUrl}/${publicId}`);
  }

  create(payload: CreateClassGroupRequest): Observable<ClassGroup> {
    return this.http.post<ClassGroup>(this.baseUrl, payload);
  }

  update(publicId: string, payload: UpdateClassGroupRequest): Observable<ClassGroup> {
    return this.http.patch<ClassGroup>(`${this.baseUrl}/${publicId}`, payload);
  }

  delete(publicId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${publicId}`);
  }
}
