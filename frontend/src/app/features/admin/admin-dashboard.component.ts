import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { AdminService } from '../../core/services/admin.service';
import { UserDto } from '../../core/models/auth.models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly adminService = inject(AdminService);
  private readonly snackBar = inject(MatSnackBar);

  readonly currentUser = signal<UserDto | null>(this.authService.getCurrentUser());
  readonly admins = signal<UserDto[]>([]);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly error = signal<string | null>(null);
  readonly welcomeSubject = computed(() => this.currentUser()?.email ?? 'Administrator');
  readonly adminCount = computed(() => this.admins().length);

  readonly quickActions = [
    {
      title: 'Doctors',
      subtitle: 'Review provider accounts, specialties, and department assignments',
      icon: 'medical_services',
      disabled: true,
    },
    {
      title: 'Patients',
      subtitle: 'Manage patient records, care plans, and appointment access',
      icon: 'people',
      disabled: true,
    },
    {
      title: 'Departments',
      subtitle: 'Configure specialties, clinical teams, and care units',
      icon: 'business',
      disabled: true,
    },
    {
      title: 'System Health',
      subtitle: 'Monitor active users, appointments, and pending actions',
      icon: 'insights',
      disabled: true,
    },
  ];

  ngOnInit(): void {
    this.loadAdmins();
  }

  deleteAdmin(admin: UserDto): void {
    if (admin.id === this.currentUser()?.id) {
      this.openSnackBar('You cannot delete your own account.', 'Dismiss');
      return;
    }

    const confirmed = confirm(`Delete admin ${admin.email}? This action cannot be undone.`);
    if (!confirmed) {
      return;
    }

    this.isSaving.set(true);
    this.error.set(null);

    this.adminService.deleteAdmin(admin.id).subscribe({
      next: () => {
        this.openSnackBar('Admin account deleted.', 'Close');
        this.loadAdmins();
      },
      error: (err: HttpErrorResponse | Error) => {
        const message = err instanceof HttpErrorResponse
          ? err.error?.message || err.error?.error || `Error ${err.status}`
          : err.message;
        this.error.set(message ?? 'Unable to delete admin');
        this.isSaving.set(false);
      },
    });
  }

  private loadAdmins(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.adminService.getAdmins().subscribe({
      next: (admins) => this.admins.set(admins),
      error: (err: HttpErrorResponse | Error) => {
        const message = err instanceof HttpErrorResponse
          ? err.error?.message || err.error?.error || `Error ${err.status}`
          : err.message;
        this.error.set(message ?? 'Unable to load admin accounts');
      },
      complete: () => this.isLoading.set(false),
    });
  }

  private openSnackBar(message: string, action: string): void {
    this.snackBar.open(message, action, { duration: 3000 });
  }
}
