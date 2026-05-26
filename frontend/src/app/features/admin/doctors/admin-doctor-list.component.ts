import { Component, inject, OnInit, signal, computed, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DoctorService } from '../../../core/services/doctor.service';
import { DoctorDto, DepartmentDto } from '../../../core/models/doctor.models';
import { HttpErrorResponse } from '@angular/common/http';
import { AdminDoctorEditDialogComponent } from './admin-doctor-edit-dialog.component';

@Component({
  selector: 'app-admin-doctor-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './admin-doctor-list.component.html',
  styleUrls: ['./admin-doctor-list.component.scss'],
})
export class AdminDoctorListComponent implements OnInit {
  private readonly doctorService = inject(DoctorService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  readonly doctors = signal<DoctorDto[]>([]);
  readonly departments = signal<DepartmentDto[]>([]);
  readonly totalElements = signal(0);
  readonly pageSize = signal(10);
  readonly pageIndex = signal(0);
  readonly isLoading = signal(false);
  readonly error = signal<string | null>(null);

  readonly selectedDepartment = signal<number | undefined>(undefined);
  readonly showDeactivated = signal(false);

  displayedColumns: string[] = ['name', 'specialization', 'department', 'status', 'actions'];

  ngOnInit(): void {
    this.loadDepartments();
    this.loadDoctors();
  }

  loadDepartments(): void {
    this.doctorService.getDepartments().subscribe({
      next: (deps) => this.departments.set(deps),
      error: (err: HttpErrorResponse) => this.snackBar.open('Failed to load departments', 'Close'),
    });
  }

  loadDoctors(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.doctorService.getDoctors(
      this.selectedDepartment(),
      this.showDeactivated(),
      { page: this.pageIndex(), size: this.pageSize() }
    ).subscribe({
      next: (response) => {
        this.doctors.set(response.content);
        this.totalElements.set(response.totalElements);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message || 'Failed to load doctors');
      },
      complete: () => this.isLoading.set(false),
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadDoctors();
  }

  onDepartmentChange(deptId: number | undefined): void {
    this.selectedDepartment.set(deptId);
    this.pageIndex.set(0);
    this.loadDoctors();
  }

  toggleDeactivated(show: boolean): void {
    this.showDeactivated.set(show);
    this.pageIndex.set(0);
    this.loadDoctors();
  }

  toggleDoctorStatus(doctor: DoctorDto): void {
    const observable = doctor.active
      ? this.doctorService.deactivateDoctor(doctor.id)
      : this.doctorService.activateDoctor(doctor.id);

    observable.subscribe({
      next: () => {
        this.snackBar.open(`Doctor ${doctor.active ? 'deactivated' : 'activated'} successfully`, 'Close', { duration: 3000 });
        this.loadDoctors();
      },
      error: (err: HttpErrorResponse) => {
        this.snackBar.open(err.error?.message || 'Failed to update doctor status', 'Close');
      }
    });
  }

  openEditDialog(doctor: DoctorDto): void {
    const dialogRef = this.dialog.open(AdminDoctorEditDialogComponent, {
      width: '500px',
      data: { doctor, departments: this.departments() }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadDoctors();
      }
    });
  }
}
