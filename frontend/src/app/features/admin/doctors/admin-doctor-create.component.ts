import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DoctorService } from '../../../core/services/doctor.service';
import { DoctorRegistrationDto, DepartmentDto } from '../../../core/models/doctor.models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-admin-doctor-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './admin-doctor-create.component.html',
  styleUrls: ['./admin-doctor-create.component.scss'],
})
export class AdminDoctorCreateComponent implements OnInit {
  private readonly doctorService = inject(DoctorService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly isSaving = signal(false);
  readonly error = signal<string | null>(null);
  readonly departments = signal<DepartmentDto[]>([]);

  readonly durations = [
    { value: 'MINUTES_15', label: '15 Minutes' },
    { value: 'MINUTES_30', label: '30 Minutes' },
    { value: 'MINUTES_45', label: '45 Minutes' },
    { value: 'MINUTES_60', label: '60 Minutes' },
  ];

  readonly specializations = [
    { value: 'GENERAL_PRACTICE', label: 'General Practice' },
    { value: 'CARDIOLOGY', label: 'Cardiology' },
    { value: 'PEDIATRICS', label: 'Pediatrics' },
    { value: 'ORTHOPEDICS', label: 'Orthopedics' },
    { value: 'DERMATOLOGY', label: 'Dermatology' },
    { value: 'NEUROLOGY', label: 'Neurology' },
    { value: 'GYNECOLOGY', label: 'Gynecology' },
    { value: 'PSYCHIATRY', label: 'Psychiatry' },
    { value: 'ONCOLOGY', label: 'Oncology' },
    { value: 'RADIOLOGY', label: 'Radiology' },
    { value: 'OPHTHALMOLOGY', label: 'Ophthalmology' },
    { value: 'DENTISTRY', label: 'Dentistry' },
    { value: 'SURGERY', label: 'Surgery' },
    { value: 'UROLOGY', label: 'Urology' },
    { value: 'ENT', label: 'Ear, Nose, and Throat' },
  ];

  readonly doctorForm = this.fb.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    specialization: ['', [Validators.required]],
    departmentId: [null as number | null, [Validators.required]],
    appointmentDuration: ['MINUTES_30', [Validators.required]],
  });

  ngOnInit(): void {
    this.loadDepartments();
  }

  private loadDepartments(): void {
    this.doctorService.getDepartments().subscribe({
      next: (deps) => this.departments.set(deps),
      error: () => this.snackBar.open('Failed to load departments', 'Close'),
    });
  }

  onSubmit(): void {
    if (this.doctorForm.invalid) {
      this.doctorForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.error.set(null);

    const registration = this.doctorForm.getRawValue() as DoctorRegistrationDto;
    this.doctorService.registerDoctor(registration).subscribe({
      next: () => {
        this.snackBar.open('Doctor account created successfully.', 'Close', { duration: 3000 });
        this.router.navigate(['/admin/doctors']);
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const validationErrors = err.error?.validationErrors || err.error;
        if (err.status === 400 && typeof validationErrors === 'object') {
          Object.keys(validationErrors).forEach((key) => {
            const control = this.doctorForm.get(key);
            if (control && typeof validationErrors[key] === 'string') {
              control.setErrors({ serverError: validationErrors[key] });
              control.markAsTouched();
            }
          });
          this.error.set('Validation failed. Please correct the highlighted fields.');
        } else {
          this.error.set(err.error?.message || 'Failed to register doctor');
        }
      },
    });
  }
}
