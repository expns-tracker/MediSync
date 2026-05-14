export interface CredentialsDto {
  email: string;
  password: string;
}

export interface AuthTokenDto {
  token: string;
}

export interface PatientRegistrationDto {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber: string;
  dateOfBirth: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  allergyIds?: number[];
  address?: string;
  city?: string;
  county?: string;
  country?: string;
}

export interface UserDto {
  id: number;
  email: string;
  role: 'ADMIN' | 'DOCTOR' | 'PATIENT';
  active: boolean;
  patientId?: number;
  doctorId?: number;
}

export interface DecodedToken {
  sub: string;
  role: string;
  userId: number;
  patientId?: number;
  doctorId?: number;
  iat: number;
  exp: number;
}
