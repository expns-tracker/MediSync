import type { DoctorDto } from './doctor.models';
import type { PatientDto } from './patient.models';

export interface MedicalRecordDto {
  id: number;
  diagnosis: string;
  treatmentPlan?: string;
  prescription?: string;
  appointmentId?: number;
  appointmentTime?: string;
  doctorId?: number;
  doctorName?: string;
  patientId?: number;
  patientName?: string;
  createdAt?: string;
  updatedAt?: string;
}

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
  patient: PatientDto;
  createdAt?: string;
  updatedAt?: string;
  medicalRecord?: MedicalRecordDto;
}
