export interface StatisticsDto {
  totalPatients: number;
  totalDoctors: number;
  totalAppointments: number;
  totalDepartments: number;
  appointmentsByStatus: { [key: string]: number };
  doctorsByDepartment: { [key: string]: number };
}
