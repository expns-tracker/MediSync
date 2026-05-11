import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { AllergyService } from '../../../core/services/allergy.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { AllergyDto } from '../../../core/models/allergy.models';
import { PatientUpdateDto } from '../../../core/models/patient.models';

@Component({
  selector: 'app-patient-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
  ],
  templateUrl: './patient-profile.component.html',
  styleUrls: ['./patient-profile.component.scss'],
})
export class PatientProfileComponent implements OnInit {
  profileForm!: FormGroup;
  allergies = signal<AllergyDto[]>([]);
  isLoading = signal(false);
  isSaving = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private patientService: PatientService,
    private allergyService: AllergyService
  ) {}

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(1)]],
      lastName: ['', [Validators.required, Validators.minLength(1)]],
      phoneNumber: ['', [Validators.required, Validators.minLength(1)]],
      dateOfBirth: ['', [Validators.required]],
      gender: ['', [Validators.required]],
      allergyIds: [[]],
      address: [''],
      city: [''],
      county: [''],
      country: [''],
    });

    const patientId = this.authService.getPatientId();
    if (!patientId) {
      this.errorMessage.set('Unable to load profile. Please sign out and sign in again.');
      return;
    }

    this.loadAllergies();
    this.loadProfile(patientId);
  }

  private loadAllergies(): void {
    this.allergyService.getAllergies().subscribe({
      next: (allergies) => this.allergies.set(allergies),
      error: (error: Error) => this.errorMessage.set(error.message),
    });
  }

  private loadProfile(patientId: number): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.patientService.getPatientById(patientId).subscribe({
      next: (patient) => {
        this.profileForm.patchValue({
          firstName: patient.firstName ?? '',
          lastName: patient.lastName ?? '',
          phoneNumber: patient.phoneNumber ?? '',
          dateOfBirth: patient.dateOfBirth ?? '',
          gender: patient.gender ?? '',
          allergyIds: patient.allergyIds ?? [],
          address: patient.address ?? '',
          city: patient.city ?? '',
          county: patient.county ?? '',
          country: patient.country ?? '',
        });
        this.isLoading.set(false);
      },
      error: (error: Error) => {
        this.isLoading.set(false);
        this.errorMessage.set(error.message);
      },
    });
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const patientId = this.authService.getPatientId();
    if (!patientId) {
      this.errorMessage.set('Unable to save profile. Please sign out and sign in again.');
      return;
    }

    const update: PatientUpdateDto = { ...this.profileForm.value };
    this.isSaving.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    this.patientService.updatePatient(patientId, update).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.successMessage.set('Your profile has been updated.');
      },
      error: (error: Error) => {
        this.isSaving.set(false);
        this.errorMessage.set(error.message);
      },
    });
  }

  toggleAllergy(allergyId: number, checked: boolean): void {
    const selected = [...(this.profileForm.get('allergyIds')?.value ?? [])] as number[];
    const index = selected.indexOf(allergyId);

    if (checked && index === -1) {
      selected.push(allergyId);
    } else if (!checked && index !== -1) {
      selected.splice(index, 1);
    }

    this.profileForm.get('allergyIds')?.setValue(selected);
  }

  isAllergySelected(allergyId: number): boolean {
    return (this.profileForm.get('allergyIds')?.value ?? []).includes(allergyId);
  }

  getSelectedAllergies(): AllergyDto[] {
    const selectedIds = this.profileForm.get('allergyIds')?.value ?? [];
    return this.allergies().filter(allergy => selectedIds.includes(allergy.id));
  }
}
