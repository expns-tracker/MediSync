import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DoctorScheduleDto, DoctorScheduleCreateDto } from '../models/doctor-schedule.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class DoctorScheduleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getDoctorSchedules(doctorId: number): Observable<DoctorScheduleDto[]> {
    return this.http
      .get<DoctorScheduleDto[]>(`${this.baseUrl}/doctors/${doctorId}/schedules`)
      .pipe(catchError(this.handleError));
  }

  createSchedule(doctorId: number, schedule: DoctorScheduleCreateDto): Observable<DoctorScheduleDto> {
    return this.http
      .post<DoctorScheduleDto>(`${this.baseUrl}/doctors/${doctorId}/schedules`, schedule)
      .pipe(catchError(this.handleError));
  }

  updateSchedule(doctorId: number, scheduleId: number, schedule: DoctorScheduleCreateDto): Observable<DoctorScheduleDto> {
    return this.http
      .put<DoctorScheduleDto>(`${this.baseUrl}/doctors/${doctorId}/schedules/${scheduleId}`, schedule)
      .pipe(catchError(this.handleError));
  }

  deleteSchedule(doctorId: number, scheduleId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/doctors/${doctorId}/schedules/${scheduleId}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    const message = error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
