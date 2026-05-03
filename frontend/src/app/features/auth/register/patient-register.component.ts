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
import { CommonModule } from '@angular/common';
import { PatientRegistrationDto } from '../../../core/models/auth.models';

@Component({
  selector: 'app-patient-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './patient-register.component.html',
  styleUrls: ['./patient-register.component.scss'],
})
export class PatientRegisterComponent implements OnInit {
  registrationForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  private passwordRegex =
    /^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&()+=_-])(?=\S+$).{8,}$/;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
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
    });
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
}
