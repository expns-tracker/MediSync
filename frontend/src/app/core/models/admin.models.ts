export interface AdminRegistrationDto {
  email: string;
  password: string;
}

export interface AdminUpdateDto {
  email?: string;
  password?: string;
}
