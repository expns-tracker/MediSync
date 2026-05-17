import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AppointmentBookDto, AppointmentDto } from '../models/appointment.models';
import { environment } from '../../../environments/environment';

interface PageResponse<T> {
  content: T[];
}

export interface MedicalRecordCreateDto {
  diagnosis: string;
  treatmentPlan?: string;
  prescription?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AppointmentService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  bookAppointment(appointment: AppointmentBookDto): Observable<AppointmentDto> {
    return this.http
      .post<AppointmentDto>(`${this.baseUrl}/appointments`, appointment)
      .pipe(catchError((error) => this.handleError(error)));
  }

  getPatientAppointments(patientId: number): Observable<AppointmentDto[]> {
    return this.http
      .get<PageResponse<AppointmentDto> | AppointmentDto[]>(`${this.baseUrl}/patients/${patientId}/appointments`)
      .pipe(
        map((response) =>
          Array.isArray(response) ? response : response.content ?? []
        ),
        catchError((error) => this.handleError(error))
      );
  }

  cancelAppointment(appointmentId: number): Observable<AppointmentDto> {
    return this.http
      .put<AppointmentDto>(`${this.baseUrl}/appointments/${appointmentId}/cancel`, {})
      .pipe(catchError((error) => this.handleError(error)));
  }

  markNoShow(appointmentId: number): Observable<AppointmentDto> {
    return this.http
      .put<AppointmentDto>(`${this.baseUrl}/appointments/${appointmentId}/no-show`, {})
      .pipe(catchError((error) => this.handleError(error)));
  }

  completeAppointment(appointmentId: number, medicalRecord: MedicalRecordCreateDto): Observable<any> {
    return this.http
      .post(`${this.baseUrl}/appointments/${appointmentId}/complete`, medicalRecord)
      .pipe(catchError((error) => this.handleError(error)));
  }

  updateMedicalRecord(medicalRecordId: number, medicalRecord: MedicalRecordCreateDto): Observable<any> {
    return this.http
      .put(`${this.baseUrl}/medical-records/${medicalRecordId}`, medicalRecord)
      .pipe(catchError((error) => this.handleError(error)));
  }

  deleteMedicalRecord(medicalRecordId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/medical-records/${medicalRecordId}`)
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse) {
    const message =
      error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
