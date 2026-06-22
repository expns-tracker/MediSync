import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';
import { ReportingService } from '../../core/services/reporting.service';
import { StatisticsDto } from '../../core/models/statistics.models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss'],
})
export class LandingComponent implements OnInit, OnDestroy {
  private authSubscription?: Subscription;
  stats?: StatisticsDto;

  constructor(
    private authService: AuthService,
    private reportingService: ReportingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authSubscription = this.authService.isAuthenticated$.subscribe(
      (isAuth) => {
        if (isAuth) {
          this.router.navigate(['/home']);
        }
      }
    );

    this.loadStats();
  }

  loadStats(): void {
    this.reportingService.getPublicStats().subscribe({
      next: (data) => this.stats = data,
      error: (err) => console.error('Failed to load stats', err)
    });
  }

  ngOnDestroy(): void {
    this.authSubscription?.unsubscribe();
  }
}
