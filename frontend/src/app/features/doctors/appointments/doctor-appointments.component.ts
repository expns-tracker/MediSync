import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentDto } from '../../../core/models/appointment.models';
import { AppointmentService } from '../../../core/services/appointment.service';
import { MedicalRecordDialogComponent } from './medical-record-dialog.component';

@Component({
  selector: 'app-doctor-appointments',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTabsModule,
  ],
  templateUrl: './doctor-appointments.component.html',
  styleUrls: ['./doctor-appointments.component.scss'],
})
export class DoctorAppointmentsComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private appointmentService = inject(AppointmentService);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  appointments = signal<AppointmentDto[]>([]);
  scheduledAppointments = signal<AppointmentDto[]>([]);
  completedAppointments = signal<AppointmentDto[]>([]);

  ngOnInit() {
    this.loadAppointments();
  }

  private loadAppointments() {
    const doctorId = this.authService.getDoctorId();
    if (!doctorId) {
      console.error('Doctor ID was not found in the authenticated user token.');
      return;
    }

    this.doctorService.getDoctorAppointments(doctorId).subscribe({
      next: (response) => {
        this.appointments.set(response.content);
        this.categorizeAppointments(response.content);
      },
      error: (error) => {
        console.error('Failed to load appointments:', error);
      },
    });
  }

  private categorizeAppointments(appointments: AppointmentDto[]) {
    const scheduled = appointments.filter(appt => appt.status === 'SCHEDULED');
    const completed = appointments.filter(appt => appt.status === 'COMPLETED');

    this.scheduledAppointments.set(scheduled);
    this.completedAppointments.set(completed);
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'SCHEDULED': return 'primary';
      case 'COMPLETED': return 'accent';
      case 'CANCELLED': return 'warn';
      default: return '';
    }
  }

  onCompleteAppointment(appointment: AppointmentDto) {
    const dialogRef = this.dialog.open(MedicalRecordDialogComponent, {
      width: '600px',
      data: { appointment },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Refresh appointments after medical record is saved
        this.loadAppointments();
      }
    });
  }

  onMarkNoShow(appointment: AppointmentDto) {
    this.appointmentService.markNoShow(appointment.id).subscribe({
      next: () => {
        this.snackBar.open('Appointment marked as no-show', 'Close', { duration: 3000 });
        this.loadAppointments();
      },
      error: (error) => {
        this.snackBar.open(error.message || 'Failed to mark appointment as no-show', 'Close', { duration: 3000 });
      },
    });
  }

  onViewMedicalRecord(appointment: AppointmentDto) {
    this.dialog.open(MedicalRecordDialogComponent, {
      width: '600px',
      data: { appointment, readonly: true },
    });
  }

  onEditMedicalRecord(appointment: AppointmentDto) {
    const dialogRef = this.dialog.open(MedicalRecordDialogComponent, {
      width: '600px',
      data: { appointment, readonly: false },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadAppointments();
      }
    });
  }
}