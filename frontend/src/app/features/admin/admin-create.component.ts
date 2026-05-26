import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AdminService } from '../../core/services/admin.service';
import { AdminRegistrationDto } from '../../core/models/admin.models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-admin-create',
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
    MatSnackBarModule,
  ],
  templateUrl: './admin-create.component.html',
  styleUrls: ['./admin-create.component.scss'],
})
export class AdminCreateComponent {
  private readonly adminService = inject(AdminService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly isSaving = signal(false);
  readonly error = signal<string | null>(null);

  readonly adminForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  readonly submitLabel = computed(() => 'Create admin');

  onSubmit(): void {
    if (this.adminForm.invalid) {
      this.adminForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.error.set(null);

    const registration = this.adminForm.getRawValue() as AdminRegistrationDto;
    this.adminService.registerAdmin(registration).subscribe({
      next: () => {
        this.snackBar.open('Admin created successfully.', 'Close', { duration: 3000 });
        this.router.navigate(['/admin/dashboard']);
      },
      error: (err: HttpErrorResponse | Error) => {
        this.isSaving.set(false);

        if (err instanceof HttpErrorResponse && err.status === 400) {
          const body = err.error;
          // Check for validationErrors object or flattened errors
          const validationErrors = body?.validationErrors || body;
          
          if (validationErrors && typeof validationErrors === 'object') {
            let hasMappedError = false;
            Object.keys(validationErrors).forEach((key) => {
              const control = this.adminForm.get(key);
              if (control && typeof validationErrors[key] === 'string') {
                control.setErrors({ serverError: validationErrors[key] });
                control.markAsTouched();
                hasMappedError = true;
              }
            });

            if (hasMappedError) {
              this.error.set('Validation failed. Please correct the highlighted fields.');
              return;
            }
          }
        }

        const message = err instanceof HttpErrorResponse
          ? err.error?.message || err.error?.error || `Error ${err.status}`
          : err.message;
        this.error.set(message || 'Unable to create admin');
      },
    });
  }
}
