import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReportingService } from '../../../core/services/reporting.service';
import { StatisticsDto } from '../../../core/models/statistics.models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-metrics',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './admin-metrics.component.html',
  styleUrls: ['./admin-metrics.component.scss'],
})
export class AdminMetricsComponent implements OnInit {
  private readonly reportingService = inject(ReportingService);

  stats = signal<StatisticsDto | null>(null);
  workload = signal<any[]>([]);
  trends = signal<any[]>([]);
  distribution = signal<any[]>([]);
  
  isLoading = signal(false);

  // Helper to convert object keys to array for templates
  statusBreakdown = computed(() => {
    const s = this.stats();
    if (!s) return [];
    return Object.entries(s.appointmentsByStatus).map(([status, count]) => ({
      status,
      count,
      percentage: (count / (s.totalAppointments || 1)) * 100
    }));
  });

  maxWorkload = computed(() => {
    const wl = this.workload();
    if (!wl || wl.length === 0) return 1;
    return Math.max(...wl.map(w => w.total_appointments));
  });

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.isLoading.set(true);
    
    forkJoin({
      summary: this.reportingService.getPublicStats(),
      workload: this.reportingService.getDoctorWorkload(),
      trends: this.reportingService.getMonthlyTrends(),
      dist: this.reportingService.getDepartmentDistribution()
    }).subscribe({
      next: (data) => {
        this.stats.set(data.summary);
        this.workload.set(data.workload);
        this.trends.set(data.trends);
        this.distribution.set(data.dist);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'SCHEDULED': return 'primary';
      case 'COMPLETED': return 'accent';
      case 'CANCELLED': return 'warn';
      case 'NO_SHOW': return 'warn';
      default: return 'primary';
    }
  }
}
