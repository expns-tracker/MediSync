import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentDto } from '../../../core/models/appointment.models';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatChipsModule,
  ],
  templateUrl: './doctor-dashboard.component.html',
  styleUrls: ['./doctor-dashboard.component.scss'],
})
export class DoctorDashboardComponent implements OnInit {
  private doctorService = inject(DoctorService);
  public authService = inject(AuthService);

  appointments = signal<AppointmentDto[]>([]);
  todayAppointments = signal<AppointmentDto[]>([]);
  upcomingAppointments = signal<AppointmentDto[]>([]);

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
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const todayAppts = appointments.filter(appt => {
      const apptDate = new Date(appt.appointmentTime);
      apptDate.setHours(0, 0, 0, 0);
      return apptDate.getTime() === today.getTime() && appt.status === 'SCHEDULED';
    });

    const upcomingAppts = appointments.filter(appt => {
      const apptDate = new Date(appt.appointmentTime);
      apptDate.setHours(0, 0, 0, 0);
      return apptDate.getTime() > today.getTime() && appt.status === 'SCHEDULED';
    });

    this.todayAppointments.set(todayAppts);
    this.upcomingAppointments.set(upcomingAppts);
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'SCHEDULED': return 'primary';
      case 'COMPLETED': return 'accent';
      case 'CANCELLED': return 'warn';
      default: return '';
    }
  }
}