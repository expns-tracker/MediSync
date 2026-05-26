package com.medisync.MediSync.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class StatisticsDto {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long totalDepartments;
    private Map<String, Long> appointmentsByStatus;
    private Map<String, Long> doctorsByDepartment;
}
