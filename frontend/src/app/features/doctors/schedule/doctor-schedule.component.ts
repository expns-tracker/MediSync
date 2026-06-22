import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorScheduleDto } from '../../../core/models/doctor.models';

@Component({
  selector: 'app-doctor-schedule',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatListModule, MatProgressSpinnerModule],
  templateUrl: './doctor-schedule.component.html',
  styleUrls: ['./doctor-schedule.component.scss'],
})
export class DoctorScheduleComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private authService = inject(AuthService);

  schedules = signal<DoctorScheduleDto[]>([]);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.loadSchedule();
  }

  private loadSchedule() {
    const doctorId = this.authService.getDoctorId();
    if (!doctorId) {
      this.errorMessage.set('Unable to determine your doctor profile. Please login again.');
      return;
    }

    this.isLoading.set(true);
    this.doctorService.getDoctorSchedules(doctorId).subscribe({
      next: (schedules) => {
        this.schedules.set(this.sortSchedules(schedules));
        this.errorMessage.set(null);
      },
      error: (error) => {
        this.errorMessage.set(error.message || 'Failed to load doctor schedule.');
      },
      complete: () => this.isLoading.set(false),
    });
  }

  private sortSchedules(schedules: DoctorScheduleDto[]): DoctorScheduleDto[] {
    const order = [
      'MONDAY',
      'TUESDAY',
      'WEDNESDAY',
      'THURSDAY',
      'FRIDAY',
      'SATURDAY',
      'SUNDAY',
    ];
    return [...schedules].sort((a, b) => order.indexOf(a.dayOfWeek) - order.indexOf(b.dayOfWeek));
  }

  getDayLabel(dayOfWeek: string): string {
    return {
      MONDAY: 'Monday',
      TUESDAY: 'Tuesday',
      WEDNESDAY: 'Wednesday',
      THURSDAY: 'Thursday',
      FRIDAY: 'Friday',
      SATURDAY: 'Saturday',
      SUNDAY: 'Sunday',
    }[dayOfWeek] ?? dayOfWeek;
  }
}
