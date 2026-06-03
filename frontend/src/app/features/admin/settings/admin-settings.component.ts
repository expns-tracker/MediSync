import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';
import { AdminService } from '../../../core/services/admin.service';
import { UserDto } from '../../../core/models/auth.models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-admin-settings',
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
    MatProgressSpinnerModule,
  ],
  templateUrl: './admin-settings.component.html',
  styleUrls: ['./admin-settings.component.scss'],
})
export class AdminSettingsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly adminService = inject(AdminService);
  private readonly snackBar = inject(MatSnackBar);

  currentUser = signal<UserDto | null>(null);
  emailForm: FormGroup;
  passwordForm: FormGroup;
  isSavingEmail = signal(false);
  isSavingPassword = signal(false);

  constructor() {
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });

    this.passwordForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.currentUser.set(user);
      this.emailForm.patchValue({ email: user.email });
    }
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  updateEmail(): void {
    if (this.emailForm.invalid || !this.currentUser()) return;

    this.isSavingEmail.set(true);
    this.adminService.updateAdmin(this.currentUser()!.id, { email: this.emailForm.value.email }).subscribe({
      next: (user) => {
        this.isSavingEmail.set(false);
        this.snackBar.open('Email updated successfully. Please log in again if your session expires.', 'Close', { duration: 5000 });
        // Optionally update the stored user info
      },
      error: (err: HttpErrorResponse) => {
        this.isSavingEmail.set(false);
        this.snackBar.open(err.error?.message || 'Failed to update email', 'Close');
      }
    });
  }

  updatePassword(): void {
    if (this.passwordForm.invalid || !this.currentUser()) return;

    this.isSavingPassword.set(true);
    this.adminService.updateAdmin(this.currentUser()!.id, { password: this.passwordForm.value.password }).subscribe({
      next: () => {
        this.isSavingPassword.set(false);
        this.snackBar.open('Password updated successfully', 'Close', { duration: 3000 });
        this.passwordForm.reset();
      },
      error: (err: HttpErrorResponse) => {
        this.isSavingPassword.set(false);
        this.snackBar.open(err.error?.message || 'Failed to update password', 'Close');
      }
    });
  }
}
