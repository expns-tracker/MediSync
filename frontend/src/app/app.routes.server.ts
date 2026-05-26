import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'doctor/patients/:patientId',
    renderMode: RenderMode.Server,
  },
  {
    path: 'admin/doctors',
    renderMode: RenderMode.Server,
  },
  {
    path: 'admin/doctors/create',
    renderMode: RenderMode.Server,
  },
  {
    path: 'admin/doctors/:doctorId/schedule',
    renderMode: RenderMode.Server,
  },
  {
    path: 'admin/patients',
    renderMode: RenderMode.Server,
  },
  {
    path: 'admin/departments',
    renderMode: RenderMode.Server,
  },
  {
    path: 'admin/allergies',
    renderMode: RenderMode.Server,
  },
  {
    path: 'admin/appointments',
    renderMode: RenderMode.Server,
  },
  {
    path: 'admin/settings',
    renderMode: RenderMode.Server,
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];
