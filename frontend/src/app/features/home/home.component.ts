import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { take } from 'rxjs';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit() {
    this.redirectToDashboard();
  }

  private redirectToDashboard() {
    this.authService.currentUser$.pipe(take(1)).subscribe(user => {
      if (user) {
        console.log('Redirecting user with role:', user.role);
        switch (user.role) {
          case 'PATIENT':
            this.router.navigate(['/appointments/history']);
            break;
          case 'DOCTOR':
            this.router.navigate(['/doctor/dashboard']);
            break;
          case 'ADMIN':
            this.router.navigate(['/admin/dashboard']);
            break;
          default:
            console.warn('Unknown role:', user.role);
            this.router.navigate(['/unauthorized']);
        }
      } else {
        console.warn('No user found in HomeComponent');
        this.router.navigate(['/login']);
      }
    });
  }
}