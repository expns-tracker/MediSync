import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PatientDto, PatientUpdateDto } from '../models/patient.models';
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

  updatePatient(patientId: number, profile: PatientUpdateDto): Observable<PatientDto> {
    return this.http
      .put<PatientDto>(`${this.baseUrl}/patients/${patientId}/`, profile)
      .pipe(catchError((error) => this.handleError(error)));
  }

  searchPatients(search?: string): Observable<PatientDto[]> {
    const params = search ? { params: { search } } : {};
    return this.http
      .get<PatientDto[]>(`${this.baseUrl}/patients`, params)
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse) {
    const message =
      error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
