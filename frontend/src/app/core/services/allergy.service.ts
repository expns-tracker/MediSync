import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AllergyDto, AllergyCreateDto } from '../models/allergy.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AllergyService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/allergies`;

  getAllergies(): Observable<AllergyDto[]> {
    return this.http
      .get<AllergyDto[]>(this.baseUrl)
      .pipe(catchError(this.handleError));
  }

  getAllergyById(id: number): Observable<AllergyDto> {
    return this.http
      .get<AllergyDto>(`${this.baseUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  createAllergy(allergy: AllergyCreateDto): Observable<AllergyDto> {
    return this.http
      .post<AllergyDto>(this.baseUrl, allergy)
      .pipe(catchError(this.handleError));
  }

  updateAllergy(id: number, allergy: AllergyCreateDto): Observable<AllergyDto> {
    return this.http
      .put<AllergyDto>(`${this.baseUrl}/${id}`, allergy)
      .pipe(catchError(this.handleError));
  }

  deleteAllergy(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    const message =
      error.error?.message || error.error?.error || `HTTP Error: ${error.status}`;
    return throwError(() => new Error(message));
  }
}
