import { Routes } from '@angular/router';
import { LandingComponent } from './features/landing/landing.component';
import { LoginComponent } from './features/auth/login/login.component';
import { PatientRegisterComponent } from './features/auth/register/patient-register.component';
import { HomeComponent } from './features/home/home.component';
import { AuthGuard, GuestGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    component: LandingComponent,
    canActivate: [GuestGuard],
  },
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [GuestGuard],
  },
  {
    path: 'register',
    component: PatientRegisterComponent,
    canActivate: [GuestGuard],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
