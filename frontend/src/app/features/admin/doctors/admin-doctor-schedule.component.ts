import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpErrorResponse } from '@angular/common/http';
import { DoctorScheduleService } from '../../../core/services/doctor-schedule.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { DoctorScheduleDto } from '../../../core/models/doctor-schedule.models';
import { DoctorDto } from '../../../core/models/doctor.models';
import { AdminDoctorScheduleDialogComponent } from './admin-doctor-schedule-dialog.component';

@Component({
  selector: 'app-admin-doctor-schedule',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './admin-doctor-schedule.component.html',
  styleUrls: ['./admin-doctor-schedule.component.scss'],
})
export class AdminDoctorScheduleComponent implements OnInit {
  private readonly scheduleService = inject(DoctorScheduleService);
  private readonly doctorService = inject(DoctorService);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  doctor = signal<DoctorDto | null>(null);
  schedules = signal<DoctorScheduleDto[]>([]);
  isLoading = signal(false);
  doctorId: number = 0;

  displayedColumns: string[] = ['dayOfWeek', 'startTime', 'endTime', 'actions'];

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('doctorId');
    if (idParam) {
      this.doctorId = +idParam;
      this.loadDoctorInfo();
      this.loadSchedules();
    }
  }

  loadDoctorInfo(): void {
    this.doctorService.getDoctorById(this.doctorId).subscribe({
      next: (data: DoctorDto) => this.doctor.set(data),
      error: (err: HttpErrorResponse | Error) => this.snackBar.open('Failed to load doctor info', 'Close'),
    });
  }

  loadSchedules(): void {
    this.isLoading.set(true);
    this.scheduleService.getDoctorSchedules(this.doctorId).subscribe({
      next: (data) => {
        this.schedules.set(this.sortSchedules(data));
        this.isLoading.set(false);
      },
      error: (err) => {
        this.snackBar.open(err.message || 'Failed to load schedules', 'Close');
        this.isLoading.set(false);
      },
    });
  }

  openScheduleDialog(schedule?: DoctorScheduleDto): void {
    const dialogRef = this.dialog.open(AdminDoctorScheduleDialogComponent, {
      width: '450px',
      data: { doctorId: this.doctorId, schedule },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadSchedules();
      }
    });
  }

  deleteSchedule(scheduleId: number): void {
    if (confirm('Are you sure you want to remove this shift?')) {
      this.scheduleService.deleteSchedule(this.doctorId, scheduleId).subscribe({
        next: () => {
          this.snackBar.open('Shift removed successfully', 'Close', { duration: 3000 });
          this.loadSchedules();
        },
        error: (err) => this.snackBar.open(err.message || 'Failed to delete shift', 'Close'),
      });
    }
  }

  private sortSchedules(schedules: DoctorScheduleDto[]): DoctorScheduleDto[] {
    const dayOrder = {
      MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3, THURSDAY: 4, FRIDAY: 5, SATURDAY: 6, SUNDAY: 7
    };
    return schedules.sort((a, b) => {
      if (dayOrder[a.dayOfWeek] !== dayOrder[b.dayOfWeek]) {
        return dayOrder[a.dayOfWeek] - dayOrder[b.dayOfWeek];
      }
      return a.startTime.localeCompare(b.startTime);
    });
  }

  formatTime(time: string): string {
    if (!time) return '';
    // Format HH:mm:ss to HH:mm
    return time.substring(0, 5);
  }
}
