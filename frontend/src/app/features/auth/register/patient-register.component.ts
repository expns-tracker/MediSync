import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AllergyService } from '../../../core/services/allergy.service';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { AllergyDto } from '../../../core/models/allergy.models';
import { PatientRegistrationDto } from '../../../core/models/auth.models';

@Component({
  selector: 'app-patient-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
  ],
  templateUrl: './patient-register.component.html',
  styleUrls: ['./patient-register.component.scss'],
})
export class PatientRegisterComponent implements OnInit {
  registrationForm!: FormGroup;
  isLoading = false;
  isAllergiesLoading = false;
  errorMessage = '';
  allergyLoadError = '';
  successMessage = '';
  allergies: AllergyDto[] = [];

  private passwordRegex =
    /^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&()+=_-])(?=\S+$).{8,}$/;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private allergyService: AllergyService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registrationForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(1)]],
      lastName: ['', [Validators.required, Validators.minLength(1)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, this.passwordValidator.bind(this)]],
      phoneNumber: ['', [Validators.required, Validators.minLength(1)]],
      dateOfBirth: ['', [Validators.required]],
      gender: ['', [Validators.required]],
      address: [''],
      city: [''],
      county: [''],
      country: [''],
      allergyIds: [[]],
    });

    this.loadAllergies();
  }

  passwordValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null;
    }
    return this.passwordRegex.test(control.value)
      ? null
      : { passwordStrength: true };
  }

  onSubmit(): void {
    if (this.registrationForm.invalid) {
      this.registrationForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const registration: PatientRegistrationDto = this.registrationForm.value;

    this.authService.registerPatient(registration).subscribe({
      next: (response: string) => {
        this.isLoading = false;
        this.successMessage =
          response || 'Account created successfully! Redirecting to login...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error: Error) => {
        this.isLoading = false;
        this.errorMessage =
          error.message ||
          'Registration failed. Please try again or contact support.';
      },
    });
  }

  private loadAllergies(): void {
    this.isAllergiesLoading = true;
    this.allergyLoadError = '';

    this.allergyService.getAllergies().subscribe({
      next: (allergies) => {
        this.allergies = allergies;
        this.isAllergiesLoading = false;
      },
      error: (error: Error) => {
        this.allergyLoadError = error.message;
        this.isAllergiesLoading = false;
      },
    });
  }

  toggleAllergy(checked: boolean, allergyId: number): void {
    const selected = [...(this.registrationForm.get('allergyIds')?.value ?? [])] as number[];
    const index = selected.indexOf(allergyId);

    if (checked && index === -1) {
      selected.push(allergyId);
    }

    if (!checked && index !== -1) {
      selected.splice(index, 1);
    }

    this.registrationForm.get('allergyIds')?.setValue(selected);
  }

  isAllergySelected(allergyId: number): boolean {
    return (this.registrationForm.get('allergyIds')?.value ?? []).includes(allergyId);
  }

  getSelectedAllergies(): AllergyDto[] {
    const selectedIds = this.registrationForm.get('allergyIds')?.value ?? [];
    return this.allergies.filter(allergy => selectedIds.includes(allergy.id));
  }
}
