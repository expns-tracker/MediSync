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
  isExistingRecord = !!this.appointment.medicalRecord;

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
    if (!this.medicalRecordForm.valid) {
      return;
    }

    const medicalRecordData = this.medicalRecordForm.value;
    const request$ = this.isExistingRecord && this.appointment.medicalRecord?.id
      ? this.appointmentService.updateMedicalRecord(this.appointment.medicalRecord.id, medicalRecordData)
      : this.appointmentService.completeAppointment(this.appointment.id, medicalRecordData);

    request$.subscribe({
      next: () => {
        const message = this.isExistingRecord
          ? 'Medical record updated successfully'
          : 'Medical record saved successfully';
        this.snackBar.open(message, 'Close', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.error('Failed to save medical record:', error);
        this.snackBar.open('Failed to save medical record', 'Close', { duration: 3000 });
      },
    });
  }

  onDelete() {
    if (!this.appointment.medicalRecord?.id) {
      return;
    }

    this.appointmentService.deleteMedicalRecord(this.appointment.medicalRecord.id).subscribe({
      next: () => {
        this.snackBar.open('Medical record deleted successfully', 'Close', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.error('Failed to delete medical record:', error);
        this.snackBar.open('Failed to delete medical record', 'Close', { duration: 3000 });
      },
    });
  }

  onCancel() {
    this.dialogRef.close();
  }
}