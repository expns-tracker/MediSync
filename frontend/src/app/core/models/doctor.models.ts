import { PageResponse } from './pagination.models';

export interface DoctorDto {
  id: number;
  firstName: string;
  lastName: string;
  specialization: string;
  specializationLabel?: string;
  departmentId?: number;
  departmentName?: string;
  appointmentDuration?: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface DoctorUpdateDto {
  firstName: string;
  lastName: string;
  specialization: string;
  departmentId: number;
  appointmentDuration: string;
}

export interface DoctorRegistrationDto extends DoctorUpdateDto {
  email: string;
  password: string;
}

export type PageDoctorDto = PageResponse<DoctorDto>;

export interface DepartmentDto {
  id: number;
  name: string;
  description?: string;
  departmentHeadId?: number;
  departmentHeadName?: string;
}
