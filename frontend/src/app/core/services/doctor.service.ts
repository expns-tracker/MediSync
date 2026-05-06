import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DoctorDto, DepartmentDto } from '../models/doctor.models';
import { AppointmentDto } from '../models/appointment.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class DoctorService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getDoctors(departmentId?: number, deactivated = false): Observable<DoctorDto[]> {
    let params = new HttpParams().set('deactivated', String(deactivated));
    if (departmentId != null) {
      params = params.set('departmentId', String(departmentId));
    }
    return this.http
      .get<DoctorDto[]>(`${this.baseUrl}/doctors`, { params })
      .pipe(catchError((error) => this.handleError(error)));
  }

  getDepartments(): Observable<DepartmentDto[]> {
    return this.http
      .get<DepartmentDto[]>(`${this.baseUrl}/departments`)
      .pipe(catchError((error) => this.handleError(error)));
  }

  getAvailableSlots(doctorId: number, date: string): Observable<string[]> {
    const params = new HttpParams().set('date', date);
    return this.http
      .get<string[]>(`${this.baseUrl}/doctors/${doctorId}/appointments/slots`, { params })
      .pipe(catchError((error) => this.handleError(error)));
  }

  getDoctorAppointments(doctorId: number): Observable<AppointmentDto[]> {
    return this.http
      .get<AppointmentDto[]>(`${this.baseUrl}/doctors/${doctorId}/appointments`)
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse) {
    const message =
      error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
