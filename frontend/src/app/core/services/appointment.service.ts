import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AppointmentBookDto, AppointmentDto } from '../models/appointment.models';
import { PageRequest, PageResponse } from '../models/pagination.models';
import { environment } from '../../../environments/environment';

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

  getAppointments(
    status?: string,
    search?: string,
    pageable: PageRequest = { page: 0, size: 20 }
  ): Observable<PageResponse<AppointmentDto>> {
    let params = new HttpParams()
      .set('page', pageable.page.toString())
      .set('size', pageable.size.toString());

    if (status) params = params.set('status', status);
    if (search) params = params.set('search', search);
    if (pageable.sort) {
      pageable.sort.forEach((s) => (params = params.append('sort', s)));
    }

    return this.http
      .get<PageResponse<AppointmentDto>>(`${this.baseUrl}/appointments`, { params })
      .pipe(catchError((error) => this.handleError(error)));
  }

  bookAppointment(appointment: AppointmentBookDto): Observable<AppointmentDto> {
    return this.http
      .post<AppointmentDto>(`${this.baseUrl}/appointments`, appointment)
      .pipe(catchError((error) => this.handleError(error)));
  }

  getPatientAppointments(
    patientId: number,
    timeframe: string = 'all',
    pageable: PageRequest = { page: 0, size: 50 }
  ): Observable<PageResponse<AppointmentDto>> {
    const params = new HttpParams()
      .set('timeframe', timeframe)
      .set('page', pageable.page.toString())
      .set('size', pageable.size.toString());

    return this.http
      .get<PageResponse<AppointmentDto>>(`${this.baseUrl}/patients/${patientId}/appointments`, { params })
      .pipe(catchError((error) => this.handleError(error)));
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

  private handleError(error: HttpErrorResponse) {
    const message =
      error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
