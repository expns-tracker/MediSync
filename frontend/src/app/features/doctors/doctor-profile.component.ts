import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { DoctorService } from '../../core/services/doctor.service';
import { AuthService } from '../../core/services/auth.service';
import { DoctorDto } from '../../core/models/doctor.models';

@Component({
  selector: 'app-doctor-profile',
  standalone: true,
  imports: [CommonModule, RouterModule, MatCardModule, MatIconModule, MatProgressSpinnerModule, MatButtonModule],
  templateUrl: './doctor-profile.component.html',
  styleUrls: ['./doctor-profile.component.scss'],
})
export class DoctorProfileComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private authService = inject(AuthService);

  doctor = signal<DoctorDto | null>(null);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.loadDoctorProfile();
  }

  private loadDoctorProfile() {
    const doctorId = this.authService.getDoctorId();
    if (!doctorId) {
      this.errorMessage.set('Unable to resolve your doctor profile. Please log in again.');
      return;
    }

    this.isLoading.set(true);
    this.doctorService.getDoctorById(doctorId).subscribe({
      next: (doctor) => {
        this.doctor.set(doctor);
        this.errorMessage.set(null);
      },
      error: (error) => {
        this.errorMessage.set(error.message || 'Unable to load doctor profile.');
      },
      complete: () => this.isLoading.set(false),
    });
  }
}
