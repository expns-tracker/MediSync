import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';
import { UserDto } from '../../core/models/auth.models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent {
  private readonly authService = inject(AuthService);
  readonly currentUser = signal<UserDto | null>(this.authService.getCurrentUser());
  readonly welcomeSubject = computed(() => this.currentUser()?.email ?? 'Administrator');

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
}
