import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AllergyDto } from '../models/allergy.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AllergyService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllergies(): Observable<AllergyDto[]> {
    return this.http
      .get<AllergyDto[]>(`${this.baseUrl}/allergies`)
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse) {
    const message =
      error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
