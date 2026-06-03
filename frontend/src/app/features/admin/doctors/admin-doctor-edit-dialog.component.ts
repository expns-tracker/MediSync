import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DoctorService } from '../../../core/services/doctor.service';
import { DoctorDto, DepartmentDto, DoctorUpdateDto } from '../../../core/models/doctor.models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-admin-doctor-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
  ],
  templateUrl: './admin-doctor-edit-dialog.component.html',
  styleUrls: ['./admin-doctor-edit-dialog.component.scss'],
})
export class AdminDoctorEditDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly doctorService = inject(DoctorService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<AdminDoctorEditDialogComponent>);
  readonly data = inject<{ doctor: DoctorDto; departments: DepartmentDto[] }>(MAT_DIALOG_DATA);

  readonly isSaving = signal(false);
  readonly error = signal<string | null>(null);

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
    firstName: [this.data.doctor.firstName, [Validators.required]],
    lastName: [this.data.doctor.lastName, [Validators.required]],
    specialization: [this.data.doctor.specialization, [Validators.required]],
    departmentId: [this.data.doctor.departmentId, [Validators.required]],
    appointmentDuration: [this.mapDurationToEnum(this.data.doctor.appointmentDuration), [Validators.required]],
  });

  private mapDurationToEnum(minutes?: number): string {
    if (!minutes) return 'MINUTES_30';
    return `MINUTES_${minutes}`;
  }

  onSubmit(): void {
    if (this.doctorForm.invalid) {
      this.doctorForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.error.set(null);

    const update = this.doctorForm.getRawValue() as DoctorUpdateDto;
    this.doctorService.updateDoctor(this.data.doctor.id, update).subscribe({
      next: () => {
        this.snackBar.open('Doctor updated successfully.', 'Close', { duration: 3000 });
        this.dialogRef.close(true);
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
          this.error.set(err.error?.message || 'Failed to update doctor');
        }
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
