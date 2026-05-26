import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DoctorDto, DepartmentDto, PageDoctorDto, DoctorUpdateDto, DoctorRegistrationDto } from '../models/doctor.models';
import { AppointmentDto } from '../models/appointment.models';
import { PageRequest } from '../models/pagination.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class DoctorService {
  private readonly baseUrl = environment.apiUrl;
  private readonly http = inject(HttpClient);

  getDoctors(
    departmentId?: number,
    deactivated = false,
    pageRequest: PageRequest = { page: 0, size: 10 }
  ): Observable<PageDoctorDto> {
    let params = new HttpParams()
      .set('deactivated', String(deactivated))
      .set('page', String(pageRequest.page))
      .set('size', String(pageRequest.size));

    if (departmentId != null) {
      params = params.set('departmentId', String(departmentId));
    }

    if (pageRequest.sort) {
      pageRequest.sort.forEach((s) => {
        params = params.append('sort', s);
      });
    }

    return this.http
      .get<PageDoctorDto>(`${this.baseUrl}/doctors`, { params })
      .pipe(catchError((error) => this.handleError(error)));
  }

  getDoctorById(doctorId: number): Observable<DoctorDto> {
    return this.http
      .get<DoctorDto>(`${this.baseUrl}/doctors/${doctorId}`)
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

  registerDoctor(registration: DoctorRegistrationDto): Observable<DoctorDto> {
    return this.http
      .post<DoctorDto>(`${this.baseUrl}/doctors`, registration)
      .pipe(catchError((error) => this.handleError(error)));
  }

  updateDoctor(doctorId: number, update: DoctorUpdateDto): Observable<DoctorDto> {
    return this.http
      .put<DoctorDto>(`${this.baseUrl}/doctors/${doctorId}`, update)
      .pipe(catchError((error) => this.handleError(error)));
  }

  activateDoctor(doctorId: number): Observable<void> {
    return this.http
      .put<void>(`${this.baseUrl}/doctors/${doctorId}/activate`, {})
      .pipe(catchError((error) => this.handleError(error)));
  }

  deactivateDoctor(doctorId: number): Observable<void> {
    return this.http
      .put<void>(`${this.baseUrl}/doctors/${doctorId}/deactivate`, {})
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse) {
    return throwError(() => error);
  }
}
