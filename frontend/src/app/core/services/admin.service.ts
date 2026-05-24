import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { UserDto } from '../models/auth.models';
import { AdminRegistrationDto } from '../models/admin.models';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private readonly baseUrl = environment.apiUrl;
  private readonly http = inject(HttpClient);

  getAdmins(): Observable<UserDto[]> {
    return this.http
      .get<UserDto[]>(`${this.baseUrl}/admins`)
      .pipe(catchError((error) => this.handleError(error)));
  }

  registerAdmin(registration: AdminRegistrationDto): Observable<UserDto> {
    return this.http
      .post<UserDto>(`${this.baseUrl}/admins`, registration)
      .pipe(catchError((error) => this.handleError(error)));
  }

  deleteAdmin(adminId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/admins/${adminId}`)
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse) {
    return throwError(() => error);
  }
}
