package com.medisync.MediSync.service;

import com.medisync.MediSync.dto.StatisticsDto;
import com.medisync.MediSync.entity.enums.AppointmentStatus;
import com.medisync.MediSync.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DepartmentRepository departmentRepository;

    public StatisticsDto getSystemStatistics() {
        Map<String, Long> apptByStatus = new HashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            apptByStatus.put(status.name(), appointmentRepository.countByStatus(status));
        }

        Map<String, Long> docsByDept = doctorRepository.findAll().stream()
                .filter(d -> d.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getDepartment().getName(),
                        Collectors.counting()
                ));

        return StatisticsDto.builder()
                .totalPatients(patientRepository.count())
                .totalDoctors(doctorRepository.count())
                .totalAppointments(appointmentRepository.count())
                .totalDepartments(departmentRepository.count())
                .appointmentsByStatus(apptByStatus)
                .doctorsByDepartment(docsByDept)
                .build();
    }
}
