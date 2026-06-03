import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { StatisticsDto } from '../models/statistics.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class StatisticsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/statistics`;

  getStatistics(): Observable<StatisticsDto> {
    return this.http
      .get<StatisticsDto>(this.baseUrl)
      .pipe(catchError((error) => throwError(() => error)));
  }
}
