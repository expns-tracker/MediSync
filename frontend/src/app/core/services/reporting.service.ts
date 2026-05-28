import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StatisticsDto } from '../models/statistics.models';

@Injectable({
  providedIn: 'root'
})
export class ReportingService {
  private apiUrl = `${environment.reportingUrl}/reports`;

  constructor(private http: HttpClient) { }

  getPublicStats(): Observable<StatisticsDto> {
    return this.http.get<StatisticsDto>(`${this.apiUrl}/summary`);
  }

  getDoctorWorkload(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/workload`);
  }

  getMonthlyTrends(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/trends`);
  }

  getDepartmentDistribution(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/distribution`);
  }
}
