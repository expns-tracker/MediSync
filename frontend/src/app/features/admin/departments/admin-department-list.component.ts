import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DepartmentService } from '../../../core/services/department.service';
import { DepartmentDto } from '../../../core/models/department.models';
import { AdminDepartmentEditDialogComponent } from './admin-department-edit-dialog.component';

@Component({
  selector: 'app-admin-department-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './admin-department-list.component.html',
  styleUrls: ['./admin-department-list.component.scss'],
})
export class AdminDepartmentListComponent implements OnInit {
  private readonly departmentService = inject(DepartmentService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  departments = signal<DepartmentDto[]>([]);
  isLoading = signal(false);

  displayedColumns: string[] = ['name', 'description', 'head', 'actions'];

  ngOnInit(): void {
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.isLoading.set(true);
    this.departmentService.getAllDepartments().subscribe({
      next: (deps) => {
        this.departments.set(deps);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.snackBar.open(err.message || 'Failed to load departments', 'Close');
        this.isLoading.set(false);
      },
    });
  }

  openEditDialog(department?: DepartmentDto): void {
    const dialogRef = this.dialog.open(AdminDepartmentEditDialogComponent, {
      width: '500px',
      data: { department },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadDepartments();
      }
    });
  }

  deleteDepartment(department: DepartmentDto): void {
    if (
      confirm(
        `Are you sure you want to delete the ${department.name} department? This action cannot be undone.`
      )
    ) {
      this.departmentService.deleteDepartment(department.id).subscribe({
        next: () => {
          this.snackBar.open('Department deleted successfully', 'Close', {
            duration: 3000,
          });
          this.loadDepartments();
        },
        error: (err) => {
          this.snackBar.open(err.message || 'Failed to delete department', 'Close');
        },
      });
    }
  }
}
