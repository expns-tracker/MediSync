export interface DepartmentDto {
  id: number;
  name: string;
  description?: string;
  departmentHeadId?: number;
  departmentHeadName?: string;
}

export interface DepartmentCreateDto {
  name: string;
  description?: string;
}

export interface DepartmentUpdateDto {
  name: string;
  description?: string;
  departmentHeadId?: number;
}
