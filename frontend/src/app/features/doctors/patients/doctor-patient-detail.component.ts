import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PatientService } from '../../../core/services/patient.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { PatientDto } from '../../../core/models/patient.models';
import { AppointmentDto } from '../../../core/models/appointment.models';
import { AppointmentListComponent } from '../../appointments/list/appointment-list.component';

@Component({
  selector: 'app-doctor-patient-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    AppointmentListComponent,
  ],
  templateUrl: './doctor-patient-detail.component.html',
  styleUrls: ['./doctor-patient-detail.component.scss'],
})
export class DoctorPatientDetailComponent implements OnInit {
  patient = signal<PatientDto | null>(null);
  appointments = signal<AppointmentDto[]>([]);
  isLoading = signal(false);
  errorMessage = signal('');

  private route = inject(ActivatedRoute);
  private patientService = inject(PatientService);
  private appointmentService = inject(AppointmentService);

  ngOnInit(): void {
    const patientId = Number(this.route.snapshot.paramMap.get('patientId'));
    if (!patientId) {
      this.errorMessage.set('Patient not found.');
      return;
    }
    this.loadPatient(patientId);
    this.loadAppointments(patientId);
  }

  private loadPatient(patientId: number): void {
    this.isLoading.set(true);
    this.patientService.getPatientById(patientId).subscribe({
      next: (patient) => {
        this.patient.set(patient);
        this.isLoading.set(false);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.isLoading.set(false);
      },
    });
  }

  private loadAppointments(patientId: number): void {
    this.appointmentService.getPatientAppointments(patientId).subscribe({
      next: (appointments) => {
        this.appointments.set(
          appointments.sort((a, b) => new Date(b.appointmentTime).getTime() - new Date(a.appointmentTime).getTime())
        );
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
      },
    });
  }
}
