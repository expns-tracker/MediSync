import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AppointmentBookDto, AppointmentDto } from '../models/appointment.models';
import { environment } from '../../../environments/environment';

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
      .get<AppointmentDto[]>(`${this.baseUrl}/patients/${patientId}/appointments`)
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse) {
    const message =
      error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
