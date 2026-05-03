import { DoctorDto } from './doctor.models';

export interface AppointmentBookDto {
  patientId: number;
  doctorId: number;
  appointmentTime: string;
  reason: string;
}

export interface AppointmentDto {
  id: number;
  appointmentTime: string;
  reason: string;
  status: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  doctor: DoctorDto;
}
