import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StatisticsService } from '../../../core/services/statistics.service';
import { StatisticsDto } from '../../../core/models/statistics.models';

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
  private readonly statsService = inject(StatisticsService);

  stats = signal<StatisticsDto | null>(null);
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

  deptBreakdown = computed(() => {
    const s = this.stats();
    if (!s) return [];
    return Object.entries(s.doctorsByDepartment).map(([name, count]) => ({
      name,
      count,
      percentage: (count / (s.totalDoctors || 1)) * 100
    }));
  });

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.isLoading.set(true);
    this.statsService.getStatistics().subscribe({
      next: (data) => {
        this.stats.set(data);
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
