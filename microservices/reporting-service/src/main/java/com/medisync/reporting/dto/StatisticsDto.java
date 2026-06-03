package com.medisync.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long totalDepartments;
    private Map<String, Long> appointmentsByStatus;
    private Map<String, Long> doctorsByDepartment;
}
