import { Routes } from '@angular/router';
import { LandingComponent } from './features/landing/landing.component';
import { LoginComponent } from './features/auth/login/login.component';
import { PatientRegisterComponent } from './features/auth/register/patient-register.component';
import { HomeComponent } from './features/home/home.component';
import { DoctorListComponent } from './features/doctors/doctor-list.component';
import { DoctorDashboardComponent } from './features/doctors/dashboard/doctor-dashboard.component';
import { AdminDashboardComponent } from './features/admin/admin-dashboard.component';
import { AdminCreateComponent } from './features/admin/admin-create.component';
import { AdminDoctorListComponent } from './features/admin/doctors/admin-doctor-list.component';
import { AdminDoctorCreateComponent } from './features/admin/doctors/admin-doctor-create.component';
import { DoctorAppointmentsComponent } from './features/doctors/appointments/doctor-appointments.component';
import { DoctorPatientListComponent } from './features/doctors/patients/doctor-patient-list.component';
import { DoctorPatientDetailComponent } from './features/doctors/patients/doctor-patient-detail.component';
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
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['PATIENT'],
    },
  },
  {
    path: 'doctor/dashboard',
    component: DoctorDashboardComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['DOCTOR'],
    },
  },
  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['ADMIN'],
    },
  },
  {
    path: 'admin/create',
    component: AdminCreateComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['ADMIN'],
    },
  },
  {
    path: 'admin/doctors',
    component: AdminDoctorListComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['ADMIN'],
    },
  },
  {
    path: 'admin/doctors/create',
    component: AdminDoctorCreateComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['ADMIN'],
    },
  },
  {
    path: 'doctor/patients',
    component: DoctorPatientListComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['DOCTOR'],
    },
  },
  {
    path: 'doctor/patients/:patientId',
    component: DoctorPatientDetailComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['DOCTOR'],
    },
  },
  {
    path: 'doctor/appointments',
    component: DoctorAppointmentsComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {
      roles: ['DOCTOR'],
    },
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
