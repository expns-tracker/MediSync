export interface DoctorScheduleDto {
  id: number;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime: string; // HH:mm:ss
  endTime: string;   // HH:mm:ss
  doctorId: number;
  doctorName?: string;
}

export interface DoctorScheduleCreateDto {
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime: string; // HH:mm
  endTime: string;   // HH:mm
}
