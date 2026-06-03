import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PatientService } from '../../../core/services/patient.service';
import { PatientDto } from '../../../core/models/patient.models';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-admin-patient-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatChipsModule,
    MatTooltipModule,
    MatSnackBarModule,
  ],
  templateUrl: './admin-patient-list.component.html',
  styleUrls: ['./admin-patient-list.component.scss'],
})
export class AdminPatientListComponent implements OnInit {
  private readonly patientService = inject(PatientService);
  private readonly snackBar = inject(MatSnackBar);

  patients = signal<PatientDto[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  pageIndex = signal(0);
  isLoading = signal(false);

  searchControl = new FormControl('');

  displayedColumns: string[] = [
    'name',
    'email',
    'phoneNumber',
    'dateOfBirth',
    'status',
    'actions',
  ];

  ngOnInit(): void {
    this.loadPatients();

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex.set(0);
        this.loadPatients();
      });
  }

  loadPatients(): void {
    this.isLoading.set(true);
    const search = this.searchControl.value || '';

    this.patientService
      .getPatients(search, {
        page: this.pageIndex(),
        size: this.pageSize(),
      })
      .subscribe({
        next: (response) => {
          this.patients.set(response.content);
          this.totalElements.set(response.totalElements);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.snackBar.open(err.message || 'Failed to load patients', 'Close');
          this.isLoading.set(false);
        },
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadPatients();
  }

  toggleStatus(patient: PatientDto): void {
    const action = patient.active
      ? this.patientService.deactivatePatient(patient.id)
      : this.patientService.activatePatient(patient.id);

    const actionName = patient.active ? 'deactivated' : 'activated';

    action.subscribe({
      next: () => {
        this.snackBar.open(`Patient ${actionName} successfully`, 'Close', {
          duration: 3000,
        });
        this.loadPatients();
      },
      error: (err) => {
        this.snackBar.open(err.message || `Failed to ${actionName} patient`, 'Close');
      },
    });
  }
}
