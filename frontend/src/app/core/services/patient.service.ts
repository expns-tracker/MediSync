import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PatientDto, PatientUpdateDto } from '../models/patient.models';
import { PageRequest, PageResponse } from '../models/pagination.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class PatientService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getPatientById(patientId: number): Observable<PatientDto> {
    return this.http
      .get<PatientDto>(`${this.baseUrl}/patients/${patientId}`)
      .pipe(catchError((error) => this.handleError(error)));
  }

  getPatients(
    search?: string,
    pageable: PageRequest = { page: 0, size: 10 }
  ): Observable<PageResponse<PatientDto>> {
    let params = new HttpParams()
      .set('page', pageable.page.toString())
      .set('size', pageable.size.toString());

    if (search) {
      params = params.set('search', search);
    }

    if (pageable.sort) {
      pageable.sort.forEach((s) => {
        params = params.append('sort', s);
      });
    }

    return this.http
      .get<PageResponse<PatientDto>>(`${this.baseUrl}/patients`, { params })
      .pipe(catchError((error) => this.handleError(error)));
  }

  updatePatient(patientId: number, profile: PatientUpdateDto): Observable<PatientDto> {
    return this.http
      .put<PatientDto>(`${this.baseUrl}/patients/${patientId}/`, profile)
      .pipe(catchError((error) => this.handleError(error)));
  }

  activatePatient(patientId: number): Observable<void> {
    return this.http
      .put<void>(`${this.baseUrl}/patients/${patientId}/activate`, {})
      .pipe(catchError((error) => this.handleError(error)));
  }

  deactivatePatient(patientId: number): Observable<void> {
    return this.http
      .put<void>(`${this.baseUrl}/patients/${patientId}/deactivate`, {})
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse) {
    const message =
      error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
