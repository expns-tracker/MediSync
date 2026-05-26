import { Component, inject, OnInit, signal } from '@angular/core';
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
import { DepartmentService } from '../../../core/services/department.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { DepartmentDto } from '../../../core/models/department.models';
import { DoctorDto } from '../../../core/models/doctor.models';

@Component({
  selector: 'app-admin-department-edit-dialog',
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
  templateUrl: './admin-department-edit-dialog.component.html',
  styleUrls: ['./admin-department-edit-dialog.component.scss'],
})
export class AdminDepartmentEditDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly departmentService = inject(DepartmentService);
  private readonly doctorService = inject(DoctorService);
  private readonly dialogRef = inject(MatDialogRef<AdminDepartmentEditDialogComponent>);
  private readonly data = inject<{ department?: DepartmentDto }>(MAT_DIALOG_DATA);

  departmentForm: FormGroup;
  isEdit = false;
  isLoading = signal(false);
  isSaving = signal(false);
  doctors = signal<DoctorDto[]>([]);

  constructor() {
    this.isEdit = !!this.data.department;
    this.departmentForm = this.fb.group({
      name: [this.data.department?.name || '', [Validators.required, Validators.minLength(2)]],
      description: [this.data.department?.description || ''],
      departmentHeadId: [this.data.department?.departmentHeadId || null],
    });
  }

  ngOnInit(): void {
    if (this.isEdit && this.data.department) {
      this.loadDepartmentDoctors(this.data.department.id);
    }
  }

  loadDepartmentDoctors(deptId: number): void {
    this.isLoading.set(true);
    // Fetching doctors for this department (active only)
    this.doctorService.getDoctors(deptId, false, { page: 0, size: 100 }).subscribe({
      next: (response) => {
        this.doctors.set(response.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.departmentForm.invalid) return;

    this.isSaving.set(true);
    const formValue = this.departmentForm.value;

    if (this.isEdit && this.data.department) {
      this.departmentService.updateDepartment(this.data.department.id, formValue).subscribe({
        next: () => {
          this.isSaving.set(false);
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isSaving.set(false);
          // Handle error (maybe show on form)
        },
      });
    } else {
      this.departmentService.createDepartment(formValue).subscribe({
        next: () => {
          this.isSaving.set(false);
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.isSaving.set(false);
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
