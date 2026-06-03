import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  MatDialogModule,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DoctorScheduleService } from '../../../core/services/doctor-schedule.service';
import { DoctorScheduleDto } from '../../../core/models/doctor-schedule.models';

@Component({
  selector: 'app-admin-doctor-schedule-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './admin-doctor-schedule-dialog.component.html',
  styleUrls: ['./admin-doctor-schedule-dialog.component.scss'],
})
export class AdminDoctorScheduleDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly scheduleService = inject(DoctorScheduleService);
  private readonly dialogRef = inject(MatDialogRef<AdminDoctorScheduleDialogComponent>);
  private readonly data = inject<{ doctorId: number; schedule?: DoctorScheduleDto }>(MAT_DIALOG_DATA);

  scheduleForm: FormGroup;
  isEdit = false;
  isSaving = signal(false);
  errorMessage = signal<string | null>(null);

  days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  constructor() {
    this.isEdit = !!this.data.schedule;
    
    // Format HH:mm:ss to HH:mm for the input type="time"
    const startTime = this.data.schedule?.startTime ? this.data.schedule.startTime.substring(0, 5) : '09:00';
    const endTime = this.data.schedule?.endTime ? this.data.schedule.endTime.substring(0, 5) : '17:00';

    this.scheduleForm = this.fb.group({
      dayOfWeek: [this.data.schedule?.dayOfWeek || 'MONDAY', [Validators.required]],
      startTime: [startTime, [Validators.required]],
      endTime: [endTime, [Validators.required]],
    });
  }

  onSubmit(): void {
    if (this.scheduleForm.invalid) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    const formValue = this.scheduleForm.value;

    if (this.isEdit && this.data.schedule) {
      this.scheduleService.updateSchedule(this.data.doctorId, this.data.schedule.id, formValue).subscribe({
        next: () => {
          this.isSaving.set(false);
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isSaving.set(false);
          this.errorMessage.set(err.message || 'Failed to update shift');
        },
      });
    } else {
      this.scheduleService.createSchedule(this.data.doctorId, formValue).subscribe({
        next: () => {
          this.isSaving.set(false);
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isSaving.set(false);
          this.errorMessage.set(err.message || 'Failed to create shift');
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
