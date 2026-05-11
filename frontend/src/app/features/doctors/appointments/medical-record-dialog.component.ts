import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AppointmentDto } from '../../../core/models/appointment.models';

@Component({
  selector: 'app-medical-record-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
  ],
  templateUrl: './medical-record-dialog.component.html',
  styleUrls: ['./medical-record-dialog.component.scss'],
})
export class MedicalRecordDialogComponent {
  private fb = inject(FormBuilder);
  private appointmentService = inject(AppointmentService);
  private snackBar = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<MedicalRecordDialogComponent>);
  private data = inject(MAT_DIALOG_DATA);

  appointment = this.data.appointment as AppointmentDto;
  readonly = this.data.readonly || false;

  medicalRecordForm: FormGroup;

  constructor() {
    this.medicalRecordForm = this.fb.group({
      diagnosis: ['', Validators.required],
      treatmentPlan: [''],
      prescription: [''],
    });

    if (this.appointment.medicalRecord) {
      this.medicalRecordForm.patchValue({
        diagnosis: this.appointment.medicalRecord.diagnosis,
        treatmentPlan: this.appointment.medicalRecord.treatmentPlan || '',
        prescription: this.appointment.medicalRecord.prescription || '',
      });
    }

    if (this.readonly) {
      this.medicalRecordForm.disable();
    }
  }

  onSave() {
    if (this.medicalRecordForm.valid) {
      const medicalRecordData = this.medicalRecordForm.value;

      this.appointmentService.completeAppointment(this.appointment.id, medicalRecordData).subscribe({
        next: () => {
          this.snackBar.open('Medical record saved successfully', 'Close', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Failed to save medical record:', error);
          this.snackBar.open('Failed to save medical record', 'Close', { duration: 3000 });
        },
      });
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}