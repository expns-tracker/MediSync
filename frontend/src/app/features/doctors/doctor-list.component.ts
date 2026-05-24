import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { distinctUntilChanged } from 'rxjs';
import { DoctorService } from '../../core/services/doctor.service';
import { AuthService } from '../../core/services/auth.service';
import { DoctorDto, DepartmentDto } from '../../core/models/doctor.models';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './doctor-list.component.html',
  styleUrls: ['./doctor-list.component.scss'],
})
export class DoctorListComponent implements OnInit {
  doctors = signal<DoctorDto[]>([]);
  departments = signal<DepartmentDto[]>([]);
  isLoading = signal(false);
  isDepartmentsLoading = signal(false);
  errorMessage = signal('');
  isDoctor = signal(false);

  private authService = inject(AuthService);

  filterForm!: FormGroup;

  constructor(private fb: FormBuilder, private doctorService: DoctorService) {
    this.filterForm = this.fb.group({
      departmentId: [null as number | null],
    });
  }

  ngOnInit(): void {
    this.isDoctor.set(this.authService.hasRole('DOCTOR'));
    this.authService.currentRole$.subscribe((role) => {
      this.isDoctor.set(role === 'DOCTOR');
    });

    this.loadDepartments();
    this.loadDoctors(null);
    this.filterForm
      .get('departmentId')
      ?.valueChanges.pipe(distinctUntilChanged())
      .subscribe((departmentId) => {
        this.loadDoctors(departmentId as number | null);
      });
  }

  private loadDepartments(): void {
    this.isDepartmentsLoading.set(true);
    this.errorMessage.set('');

    this.doctorService.getDepartments().subscribe({
      next: (departments) => {
        this.departments.set(departments);
        this.isDepartmentsLoading.set(false);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.departments.set([]);
        this.isDepartmentsLoading.set(false);
      },
    });
  }

  private loadDoctors(departmentId?: number | null): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.doctorService.getDoctors(departmentId ?? undefined, false, { page: 0, size: 100 }).subscribe({
      next: (response) => {
        this.doctors.set(response.content);
        this.isLoading.set(false);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.doctors.set([]);
        this.isLoading.set(false);
      },
    });
  }
}
