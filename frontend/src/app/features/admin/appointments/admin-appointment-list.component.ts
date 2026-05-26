import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AppointmentDto } from '../../../core/models/appointment.models';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-admin-appointment-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatPaginatorModule,
    MatChipsModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './admin-appointment-list.component.html',
  styleUrls: ['./admin-appointment-list.component.scss'],
})
export class AdminAppointmentListComponent implements OnInit {
  private readonly appointmentService = inject(AppointmentService);
  private readonly snackBar = inject(MatSnackBar);

  appointments = signal<AppointmentDto[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  isLoading = signal(false);

  searchControl = new FormControl('');
  statusControl = new FormControl('');

  displayedColumns: string[] = [
    'time',
    'patient',
    'doctor',
    'reason',
    'status',
    'actions',
  ];

  statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'SCHEDULED', label: 'Scheduled' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'CANCELLED', label: 'Cancelled' },
    { value: 'NO_SHOW', label: 'No-Show' },
  ];

  ngOnInit(): void {
    this.loadAppointments();

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex.set(0);
        this.loadAppointments();
      });

    this.statusControl.valueChanges.subscribe(() => {
      this.pageIndex.set(0);
      this.loadAppointments();
    });
  }

  loadAppointments(): void {
    this.isLoading.set(true);
    const search = this.searchControl.value || '';
    const status = this.statusControl.value || '';

    this.appointmentService
      .getAppointments(status, search, {
        page: this.pageIndex(),
        size: this.pageSize(),
        sort: ['appointmentTime,desc'],
      })
      .subscribe({
        next: (response) => {
          this.appointments.set(response.content);
          this.totalElements.set(response.totalElements);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.snackBar.open(err.message || 'Failed to load appointments', 'Close');
          this.isLoading.set(false);
        },
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadAppointments();
  }

  cancelAppointment(appointment: AppointmentDto): void {
    if (confirm('Are you sure you want to cancel this appointment?')) {
      this.appointmentService.cancelAppointment(appointment.id).subscribe({
        next: () => {
          this.snackBar.open('Appointment cancelled', 'Close', { duration: 3000 });
          this.loadAppointments();
        },
        error: (err) => this.snackBar.open(err.message, 'Close'),
      });
    }
  }

  markNoShow(appointment: AppointmentDto): void {
    if (confirm('Mark this patient as a No-Show?')) {
      this.appointmentService.markNoShow(appointment.id).subscribe({
        next: () => {
          this.snackBar.open('Marked as No-Show', 'Close', { duration: 3000 });
          this.loadAppointments();
        },
        error: (err) => this.snackBar.open(err.message, 'Close'),
      });
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'SCHEDULED': return '#6366f1';
      case 'COMPLETED': return '#10b981';
      case 'CANCELLED': return '#ef4444';
      case 'NO_SHOW': return '#f59e0b';
      default: return '#6b7280';
    }
  }
}
