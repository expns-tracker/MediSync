export interface PatientDto {
  id: number;
  firstName: string;
  lastName: string;
  email?: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  allergyIds?: number[];
  address?: string;
  city?: string;
  county?: string;
  country?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PatientUpdateDto {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  dateOfBirth: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  allergyIds?: number[];
  address?: string;
  city?: string;
  county?: string;
  country?: string;
}
