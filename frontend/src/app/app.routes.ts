import { Routes } from '@angular/router';
import { LandingComponent } from './features/landing/landing.component';
import { LoginComponent } from './features/auth/login/login.component';
import { PatientRegisterComponent } from './features/auth/register/patient-register.component';
import { HomeComponent } from './features/home/home.component';
import { DoctorListComponent } from './features/doctors/doctor-list.component';
import { AppointmentBookComponent } from './features/appointments/book/appointment-book.component';
import { UpcomingAppointmentsComponent } from './features/appointments/upcoming/upcoming-appointments.component';
import { AppointmentHistoryComponent } from './features/appointments/history/appointment-history.component';
import { PatientProfileComponent } from './features/patients/profile/patient-profile.component';
import { UnauthorizedComponent } from './features/unauthorized/unauthorized.component';
import { AuthGuard, GuestGuard, RoleGuard } from './core/guards/role.guard';

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
    path: 'doctors',
    component: DoctorListComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'appointments/book',
    component: AppointmentBookComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['PATIENT'],
    },
  },
  {
    path: 'appointments/upcoming',
    component: UpcomingAppointmentsComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['PATIENT'],
    },
  },
  {
    path: 'appointments/history',
    component: AppointmentHistoryComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['PATIENT'],
    },
  },
  {
    path: 'profile',
    component: PatientProfileComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['PATIENT'],
    },
  },
  {
    path: 'unauthorized',
    component: UnauthorizedComponent,
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
