import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { PatientService } from '../../../core/services/patient.service';
import { PatientDto } from '../../../core/models/patient.models';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-doctor-patient-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './doctor-patient-list.component.html',
  styleUrls: ['./doctor-patient-list.component.scss'],
})
export class DoctorPatientListComponent implements OnInit {
  patients = signal<PatientDto[]>([]);
  isLoading = signal(false);
  errorMessage = signal('');

  private patientService = inject(PatientService);
  private fb = inject(FormBuilder);

  searchForm = this.fb.group({
    query: [''],
  });

  ngOnInit(): void {
    this.loadPatients();
    this.searchForm
      .get('query')
      ?.valueChanges.pipe(debounceTime(250), distinctUntilChanged())
      .subscribe((query) => {
        this.loadPatients(query as string);
      });
  }

  private loadPatients(searchQuery?: string): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.patientService.searchPatients(searchQuery).subscribe({
      next: (patients) => {
        this.patients.set(patients);
        this.isLoading.set(false);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.patients.set([]);
        this.isLoading.set(false);
      },
    });
  }
}
