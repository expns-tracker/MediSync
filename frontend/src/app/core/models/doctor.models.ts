export interface DoctorDto {
  id: number;
  firstName: string;
  lastName: string;
  specialization: string;
  specializationLabel?: string;
  departmentId?: number;
  departmentName?: string;
  appointmentDuration?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface DepartmentDto {
  id: number;
  name: string;
  description?: string;
  departmentHeadId?: number;
  departmentHeadName?: string;
}
