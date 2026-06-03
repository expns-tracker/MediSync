import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  DepartmentDto,
  DepartmentCreateDto,
  DepartmentUpdateDto,
} from '../models/department.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class DepartmentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/departments`;

  getAllDepartments(): Observable<DepartmentDto[]> {
    return this.http
      .get<DepartmentDto[]>(this.baseUrl)
      .pipe(catchError(this.handleError));
  }

  getDepartmentById(id: number): Observable<DepartmentDto> {
    return this.http
      .get<DepartmentDto>(`${this.baseUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  createDepartment(department: DepartmentCreateDto): Observable<DepartmentDto> {
    return this.http
      .post<DepartmentDto>(this.baseUrl, department)
      .pipe(catchError(this.handleError));
  }

  updateDepartment(
    id: number,
    department: DepartmentUpdateDto
  ): Observable<DepartmentDto> {
    return this.http
      .put<DepartmentDto>(`${this.baseUrl}/${id}`, department)
      .pipe(catchError(this.handleError));
  }

  deleteDepartment(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    const message =
      error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
