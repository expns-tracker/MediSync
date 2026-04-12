import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { PatientRegistrationDto } from '../../../core/models/auth.models';

@Component({
  selector: 'app-patient-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="register-container">
      <div class="register-box">
        <h1>Create Patient Account</h1>
        <p class="subtitle">Join MediSync to book appointments</p>

        <form [formGroup]="registrationForm" (ngSubmit)="onSubmit()">
          <!-- Personal Information Section -->
          <h3 class="section-title">Personal Information</h3>

          <div class="form-row">
            <div class="form-group">
              <label for="firstName">First Name</label>
              <input
                id="firstName"
                type="text"
                formControlName="firstName"
                placeholder="John"
                class="form-input"
                [class.error]="
                  registrationForm.get('firstName')?.invalid &&
                  registrationForm.get('firstName')?.touched
                "
              />
              <span
                class="error-message"
                *ngIf="
                  registrationForm.get('firstName')?.invalid &&
                  registrationForm.get('firstName')?.touched
                "
              >
                First name is required
              </span>
            </div>

            <div class="form-group">
              <label for="lastName">Last Name</label>
              <input
                id="lastName"
                type="text"
                formControlName="lastName"
                placeholder="Doe"
                class="form-input"
                [class.error]="
                  registrationForm.get('lastName')?.invalid &&
                  registrationForm.get('lastName')?.touched
                "
              />
              <span
                class="error-message"
                *ngIf="
                  registrationForm.get('lastName')?.invalid &&
                  registrationForm.get('lastName')?.touched
                "
              >
                Last name is required
              </span>
            </div>
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input
              id="email"
              type="email"
              formControlName="email"
              placeholder="your@email.com"
              class="form-input"
              [class.error]="
                registrationForm.get('email')?.invalid &&
                registrationForm.get('email')?.touched
              "
            />
            <span
              class="error-message"
              *ngIf="
                registrationForm.get('email')?.invalid &&
                registrationForm.get('email')?.touched
              "
            >
              {{
                registrationForm.get('email')?.hasError('required')
                  ? 'Email is required'
                  : 'Please enter a valid email'
              }}
            </span>
          </div>

          <div class="form-group">
            <label for="phoneNumber">Phone Number</label>
            <input
              id="phoneNumber"
              type="tel"
              formControlName="phoneNumber"
              placeholder="+1 (555) 000-0000"
              class="form-input"
              [class.error]="
                registrationForm.get('phoneNumber')?.invalid &&
                registrationForm.get('phoneNumber')?.touched
              "
            />
            <span
              class="error-message"
              *ngIf="
                registrationForm.get('phoneNumber')?.invalid &&
                registrationForm.get('phoneNumber')?.touched
              "
            >
              Phone number is required
            </span>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="dateOfBirth">Date of Birth</label>
              <input
                id="dateOfBirth"
                type="date"
                formControlName="dateOfBirth"
                class="form-input"
                [class.error]="
                  registrationForm.get('dateOfBirth')?.invalid &&
                  registrationForm.get('dateOfBirth')?.touched
                "
              />
              <span
                class="error-message"
                *ngIf="
                  registrationForm.get('dateOfBirth')?.invalid &&
                  registrationForm.get('dateOfBirth')?.touched
                "
              >
                Date of birth is required
              </span>
            </div>

            <div class="form-group">
              <label for="gender">Gender</label>
              <select formControlName="gender" class="form-input">
                <option value="" disabled>Select gender</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
              <span
                class="error-message"
                *ngIf="
                  registrationForm.get('gender')?.invalid &&
                  registrationForm.get('gender')?.touched
                "
              >
                Gender is required
              </span>
            </div>
          </div>

          <!-- Address Section -->
          <h3 class="section-title">Address (Optional)</h3>

          <div class="form-group">
            <label for="address">Street Address</label>
            <input
              id="address"
              type="text"
              formControlName="address"
              placeholder="123 Main St"
              class="form-input"
            />
          </div>

          <div class="form-row three-col">
            <div class="form-group">
              <label for="city">City</label>
              <input
                id="city"
                type="text"
                formControlName="city"
                placeholder="New York"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="county">County</label>
              <input
                id="county"
                type="text"
                formControlName="county"
                placeholder="County"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="country">Country</label>
              <input
                id="country"
                type="text"
                formControlName="country"
                placeholder="USA"
                class="form-input"
              />
            </div>
          </div>

          <!-- Security Section -->
          <h3 class="section-title">Security</h3>

          <div class="form-group">
            <label for="password">Password</label>
            <input
              id="password"
              type="password"
              formControlName="password"
              placeholder="••••••••"
              class="form-input"
              [class.error]="
                registrationForm.get('password')?.invalid &&
                registrationForm.get('password')?.touched
              "
            />
            <span
              class="error-message"
              *ngIf="
                registrationForm.get('password')?.touched &&
                registrationForm.get('password')?.invalid
              "
            >
              {{
                registrationForm.get('password')?.hasError('required')
                  ? 'Password is required'
                  : 'Password must be at least 8 characters with uppercase letter, number, and special character'
              }}
            </span>
          </div>

          <button
            type="submit"
            class="btn-primary"
            [disabled]="registrationForm.invalid || isLoading"
          >
            {{ isLoading ? 'Creating Account...' : 'Create Account' }}
          </button>
        </form>

        <div class="register-footer">
          <p>
            <a routerLink="/" class="link-home">← Back to Home</a>
          </p>
          <p style="margin-top: 12px;">
            Already have an account?
            <a routerLink="/login" class="link">Sign in here</a>
          </p>
        </div>

        <div *ngIf="errorMessage" class="error-banner">
          {{ errorMessage }}
        </div>

        <div *ngIf="successMessage" class="success-banner">
          {{ successMessage }}
        </div>
      </div>
    </div>
  `,
  styles: `
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
        Ubuntu, Cantarell, sans-serif;
      padding: 20px;
    }

    .register-box {
      background: white;
      border-radius: 8px;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
      padding: 40px;
      width: 100%;
      max-width: 600px;
      max-height: 90vh;
      overflow-y: auto;
    }

    h1 {
      font-size: 28px;
      font-weight: 600;
      margin: 0 0 8px;
      color: #333;
    }

    .subtitle {
      color: #666;
      margin: 0 0 30px;
      font-size: 14px;
    }

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #666;
      margin: 30px 0 15px;
      text-transform: uppercase;
      border-bottom: 1px solid #eee;
      padding-bottom: 10px;
    }

    .section-title:first-child {
      margin-top: 0;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 25px;
    }

    .form-row.three-col {
      grid-template-columns: 1fr 1fr 1fr;
    }

    .form-group {
      margin-bottom: 15px;
    }

    label {
      display: block;
      font-size: 13px;
      font-weight: 500;
      margin-bottom: 6px;
      color: #333;
    }

    .form-input {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 13px;
      font-family: inherit;
      transition: border-color 0.2s;
    }

    .form-input:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .form-input.error {
      border-color: #e74c3c;
    }

    select.form-input {
      cursor: pointer;
      appearance: none;
      background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
      background-repeat: no-repeat;
      background-position: right 8px center;
      background-size: 20px;
      padding-right: 30px;
    }

    .error-message {
      display: block;
      font-size: 12px;
      color: #e74c3c;
      margin-top: 4px;
    }

    .btn-primary {
      width: 100%;
      padding: 12px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 4px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
      margin-top: 20px;
    }

    .btn-primary:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
    }

    .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .register-footer {
      text-align: center;
      margin-top: 20px;
      font-size: 13px;
      color: #666;
    }

    .link {
      color: #667eea;
      text-decoration: none;
      font-weight: 600;
    }

    .link:hover {
      text-decoration: underline;
    }

    .link-home {
      color: #999;
      text-decoration: none;
      font-size: 12px;
      font-weight: 500;
    }

    .link-home:hover {
      color: #667eea;
      text-decoration: underline;
    }

    .error-banner {
      margin-top: 20px;
      padding: 12px;
      background-color: #fee;
      color: #c33;
      border-left: 4px solid #c33;
      border-radius: 4px;
      font-size: 13px;
    }

    .success-banner {
      margin-top: 20px;
      padding: 12px;
      background-color: #efe;
      color: #3c3;
      border-left: 4px solid #3c3;
      border-radius: 4px;
      font-size: 13px;
    }

    @media (max-width: 600px) {
      .register-box {
        padding: 20px;
        max-width: 100%;
      }

      .form-row {
        grid-template-columns: 1fr;
      }

      .form-row.three-col {
        grid-template-columns: 1fr;
      }
    }
  `,
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

  ngOnInit() {
    this.registrationForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(1)]],
      lastName: ['', [Validators.required, Validators.minLength(1)]],
      email: ['', [Validators.required, Validators.email]],
      password: [
        '',
        [
          Validators.required,
          this.passwordValidator.bind(this),
        ],
      ],
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
