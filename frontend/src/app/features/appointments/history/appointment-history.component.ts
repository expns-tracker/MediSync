import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentListComponent } from '../list/appointment-list.component';
import { AppointmentDto } from '../../../core/models/appointment.models';

@Component({
  selector: 'app-appointment-history',
  standalone: true,
  imports: [CommonModule, RouterLink, AppointmentListComponent],
  templateUrl: './appointment-history.component.html',
  styleUrls: ['./appointment-history.component.scss'],
})
export class AppointmentHistoryComponent implements OnInit {
  appointments = signal<AppointmentDto[]>([]);
  isLoading = signal(false);
  errorMessage = signal('');

  constructor(
    private appointmentService: AppointmentService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const patientId = this.authService.getPatientId();
    if (!patientId) {
      this.errorMessage.set('Unable to load appointment history. Please sign out and sign in again.');
      return;
    }

    this.loadAppointments(patientId);
  }

  private loadAppointments(patientId: number): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.appointmentService.getPatientAppointments(patientId, 'past').subscribe({
      next: (response) => {
        const now = new Date();
        this.appointments.set(
          response.content
            .filter((appointment) =>
              appointment.status !== 'SCHEDULED' ||
              new Date(appointment.appointmentTime) < now
            )
            .sort((a, b) => new Date(b.appointmentTime).getTime() - new Date(a.appointmentTime).getTime())
        );
        this.isLoading.set(false);
      },
      error: (error: Error) => {
        this.isLoading.set(false);
        this.errorMessage.set(error.message);
      },
    });
  }

  onCancelAppointment(appointmentId: number): void {
    const patientId = this.authService.getPatientId();
    if (!patientId) {
      this.errorMessage.set('Unable to cancel appointment. Please sign out and sign in again.');
      return;
    }

    this.appointmentService.cancelAppointment(appointmentId).subscribe({
      next: () => {
        this.loadAppointments(patientId);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
      },
    });
  }
}
